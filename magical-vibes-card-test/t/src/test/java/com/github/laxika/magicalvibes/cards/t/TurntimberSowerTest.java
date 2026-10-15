package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurntimberSower.class, Forest.class, GrizzlyBears.class, Mountain.class, StoneRain.class, Millstone.class})
class TurntimberSowerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Plant when your land is put into your graveyard")
    void createsPlantWhenLandEntersGraveyard() {
        harness.addToBattlefield(player1, new TurntimberSower());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Plant"));
    }

    @Test
    @DisplayName("Sacrifices three creatures to return a target land from the graveyard")
    void sacrificesThreeCreaturesAndReturnsTargetLand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TurntimberSower());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbilityWithGraveyardTargets(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0,
                List.of(forest.getId()));
        for (Permanent creature : List.of(first, second, third)) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(first.getCard().getId(), second.getCard().getId(), third.getCard().getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(forest.getId());
    }

    @Test
    @DisplayName("Rejects a nonland graveyard target")
    void rejectsNonlandGraveyardTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TurntimberSower());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0,
                List.of(nonland.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two lands milled simultaneously create only one Plant")
    void simultaneousLandMillCreatesOnePlant() {
        harness.addToBattlefield(player1, new TurntimberSower());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plant"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milling an opponent's lands creates no Plants")
    void opponentLandMillCreatesNoPlants() {
        harness.addToBattlefield(player1, new TurntimberSower());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Plant"));
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milling only nonlands creates no Plants")
    void nonlandMillCreatesNoPlants() {
        harness.addToBattlefield(player1, new TurntimberSower());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new StoneRain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Plant"));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Sower can be one of the three sacrificed creatures")
    void canSacrificeSowerToItsOwnAbility() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TurntimberSower());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));
        for (Permanent creature : List.of(source, first, second)) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(source.getCard().getId(), first.getCard().getId(), second.getCard().getId())
                .doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Rejects a land in an opponent's graveyard")
    void rejectsOpponentsLandTarget() {
        harness.addToBattlefield(player1, new TurntimberSower());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three creatures")
    void requiresThreeCreatures() {
        harness.addToBattlefield(player1, new TurntimberSower());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
