package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RexCyberHound.class, BirdsOfParadise.class, DrudgeSkeletons.class, GrizzlyBears.class, RodOfRuin.class})
class RexCyberHoundTest extends BaseCardTest {

    @Test
    void millsDamagedPlayerAndGivesControllerTwoEnergy() {
        Permanent rex = addCreatureReady(player1, new RexCyberHound());
        rex.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void exilesTargetCreatureWithBrainCounter() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(skeletons);
        assertThat(gd.exiledCardsWithBrainCounters).containsExactly(skeletons.getId());
    }

    @Test
    void gainsActivatedAbilityFromBrainCounterCard() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setExile(player2, List.of(skeletons));
        gd.exiledCardsWithBrainCounters.add(skeletons.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreatureCard() {
        addCreatureReady(player1, new RexCyberHound());
        Card rodOfRuin = new RodOfRuin();
        harness.setGraveyard(player2, List.of(rodOfRuin));
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(rodOfRuin.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsEnergyEvenWhenDamagedPlayerHasNoCardsToMill() {
        Permanent rex = addCreatureReady(player1, new RexCyberHound());
        rex.setAttacking(true);
        harness.setLibrary(player2, List.of());

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void exilesFromOwnGraveyardAndImmediatelyGainsTheAbility() {
        Permanent rex = addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player1, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(skeletons);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(skeletons);
        assertThat(rex.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithOnlyOneEnergy() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(skeletons.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(skeletons);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(skeletons.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(skeletons.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnTheStack() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 4);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(skeletons.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(skeletons);
    }

    @Test
    void doesNotGainAbilitiesFromExiledCardsWithoutBrainCounters() {
        addCreatureReady(player1, new RexCyberHound());
        harness.setExile(player2, List.of(new DrudgeSkeletons()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losesGrantedAbilityWhenBrainCounterCardLeavesExile() {
        Permanent rex = addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setExile(player2, List.of(skeletons));
        gd.exiledCardsWithBrainCounters.add(skeletons.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(rex.getRegenerationShield()).isEqualTo(1);

        gd.removeFromExile(skeletons.getId());
        harness.setGraveyard(player2, List.of(skeletons));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rex.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void invalidatedGraveyardTargetDoesNotRefundPaidEnergy() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(skeletons));

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(skeletons);
        assertThat(gd.exiledCardsWithBrainCounters).doesNotContain(skeletons.getId());
    }

    @Test
    void gainsManaAbilitiesFromBrainCounterCards() {
        Permanent rex = addCreatureReady(player1, new RexCyberHound());
        Card birds = new BirdsOfParadise();
        harness.setExile(player2, List.of(birds));
        gd.exiledCardsWithBrainCounters.add(birds.getId());
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(manaBefore + 1);
        assertThat(rex.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
