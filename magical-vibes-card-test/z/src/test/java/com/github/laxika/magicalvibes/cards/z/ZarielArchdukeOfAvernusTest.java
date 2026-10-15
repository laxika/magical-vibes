package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BurningHands;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZarielArchdukeOfAvernus.class, HillGiantHerdgorger.class, BurningHands.class, SarkhanTheMasterless.class})
class ZarielArchdukeOfAvernusTest extends BaseCardTest {

    @Test
    @DisplayName("+1 pumps your creatures and gives them haste until end of turn")
    void plusOnePumpsOwnCreaturesAndGrantsHaste() {
        Permanent zariel = addReadyZariel(player1, 3);
        Permanent ownBears = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent opposingBears = addCreatureReady(player2, new HillGiantHerdgorger());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(zariel.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("0 creates a Devil whose death ability deals 1 damage to a target")
    void zeroCreatesDevilWithDeathTrigger() {
        addReadyZariel(player1, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent devil = findPermanents(player1, "Devil").getFirst();
        killDevil(devil);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("−6 creates a first-combat emblem that untaps a creature and adds combat")
    void ultimateCreatesFirstCombatEmblem() {
        addReadyZariel(player1, 6);
        Permanent bears = addCreatureReady(player1, new HillGiantHerdgorger());
        bears.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("+1 does not affect creatures entering after it resolves")
    void plusOneDoesNotAffectLaterCreatures() {
        addReadyZariel(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(player1, new HillGiantHerdgorger());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("+1 power and haste expire in cleanup")
    void plusOneExpiresInCleanup() {
        addReadyZariel(player1, 4);
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Devil death trigger can damage a creature")
    void devilDeathTriggerCanDamageCreature() {
        addReadyZariel(player1, 4);
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        killDevil(findPermanents(player1, "Devil").getFirst());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Devil death trigger can damage a planeswalker")
    void devilDeathTriggerCanDamagePlaneswalker() {
        Permanent zariel = addReadyZariel(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        killDevil(findPermanents(player1, "Devil").getFirst());
        harness.handlePermanentChosen(player1, zariel.getId());
        harness.passBothPriorities();

        assertThat(zariel.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Emblem does not trigger at the end of the second combat")
    void emblemDoesNotTriggerAfterSecondCombat() {
        addReadyZariel(player1, 6);
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        creature.tap();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 2;
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Emblem does not trigger during the opponent's combat")
    void emblemDoesNotTriggerOnOpponentsTurn() {
        addReadyZariel(player1, 6);
        Permanent creature = addCreatureReady(player1, new HillGiantHerdgorger());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        creature.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Emblem cannot add combat when there is no legal creature target")
    void emblemWithoutCreatureTargetDoesNotAddCombat() {
        addReadyZariel(player1, 6);
        addCreatureReady(player2, new HillGiantHerdgorger());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("+1 grants haste to Zariel herself when she is a creature")
    void plusOneGrantsHasteToAnimatedZariel() {
        Permanent zariel = addReadyZariel(player1, 4);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, zariel)).isTrue();
        assertThat(gqs.hasKeyword(gd, zariel, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zariel)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, zariel, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Emblem can target an untapped creature and leaves other creatures tapped")
    void emblemCanTargetUntappedCreature() {
        addReadyZariel(player1, 6);
        Permanent target = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent other = addCreatureReady(player1, new HillGiantHerdgorger());
        other.tap();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;
        harness.passUntil(TurnStep.END_OF_COMBAT);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(target.getId(), other.getId());
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Emblem does not add combat if its only target dies in response")
    void emblemDoesNotAddCombatWhenTargetDies() {
        addReadyZariel(player1, 6);
        Permanent target = addCreatureReady(player1, new HillGiantHerdgorger());
        target.tap();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gd.combatPhasesThisTurn = 1;
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.handlePermanentChosen(player1, target.getId());
            harness.castInstant(player1, 0, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    private void killDevil(Permanent devil) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurningHands()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyZariel(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ZarielArchdukeOfAvernus());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
