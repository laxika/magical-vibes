package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NissaAscendedAnimist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConduitOfWorlds.class, Cankerbloom.class, Forest.class, NissaAscendedAnimist.class})
class ConduitOfWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from the controller's graveyard")
    void playsLandFromGraveyard() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May cast a targeted nonland permanent from the graveyard during resolution")
    void castsTargetPermanentDuringResolution() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom bears = new Cankerbloom();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cankerbloom");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A prior spell prevents the activated ability from offering a cast")
    void priorSpellPreventsCast() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        prepareMainPhase();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Cankerbloom graveyardBears = new Cankerbloom();
        harness.setGraveyard(player1, List.of(graveyardBears));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(graveyardBears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A successful cast prevents later spells but not a land play")
    void successfulCastPreventsSpellsButNotLandPlay() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom bears = new Cankerbloom();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(bears, forest));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, 0);
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a land with the cast ability")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningCastDoesNotRestrictLaterSpells() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom target = new Cankerbloom();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cankerbloom");
        harness.assertOnBattlefield(player1, "Cankerbloom");
    }

    @Test
    void insufficientManaLeavesCardInGraveyardAndDoesNotRestrictSpells() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom target = new Cankerbloom();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cankerbloom");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom target = new Cankerbloom();
        harness.setGraveyard(player2, List.of(target));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom target = new Cankerbloom();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landPermissionDoesNotAllowASecondLandPlay() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void targetLeavingGraveyardPreventsCast() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom target = new Cankerbloom();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Cankerbloom");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
    }

    @Test
    void cannotActivateWhileAnotherSpellIsOnStack() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Cankerbloom target = new Cankerbloom();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castArtifactFromGraveyardPaysItsManaCost() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        ConduitOfWorlds target = new ConduitOfWorlds();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 4);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Conduit of Worlds")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    void compleatedUsesLifePaymentsWhenCastFromGraveyard() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        NissaAscendedAnimist nissa = new NissaAscendedAnimist();
        harness.setGraveyard(player1, List.of(nissa));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(nissa.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(findPermanent(player1, "Nissa, Ascended Animist").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(3);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
