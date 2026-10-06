package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.cards.s.ShortBow;
import com.github.laxika.magicalvibes.cards.s.ShrikeForce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RalCracklingWit.class, Divination.class, GrizzlyBears.class, ShoreUp.class, ShortBow.class, ShrikeForce.class})
class RalCracklingWitTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates an Otter that gets +1/+1 when its controller casts a noncreature spell")
    void plusOneCreatesOtterWithProwess() {
        Permanent ral = addReadyRal(2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent otter = findPermanent(player1, "Otter");
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not put a loyalty counter on Ral for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        Permanent ral = addReadyRal(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 draws three cards then discards two cards")
    void minusThreeDrawsThenDiscards() {
        Permanent ral = addReadyRal(5);
        Card first = new Divination();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("-10 draws three cards and creates an emblem that gives instant and sorcery spells storm")
    void minusTenCreatesStormEmblem() {
        addReadyRal(10);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .allMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("An artifact spell triggers both Ral and the Otter, and prowess expires at cleanup")
    void artifactTriggersLoyaltyAndTemporaryProwess() {
        Permanent ral = addReadyRal(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent otter = findPermanent(player1, "Otter");

        harness.setHand(player1, List.of(new ShortBow()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ral's loyalty trigger resolves before the instant that caused it")
    void instantAddsLoyaltyBeforeResolving() {
        Permanent ral = addReadyRal(4);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShrikeForce());
        harness.setHand(player1, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, creature.getId());
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's noncreature spell triggers neither Ral nor the Otter")
    void opponentSpellDoesNotTriggerLoyaltyOrProwess() {
        Permanent ral = addReadyRal(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent otter = findPermanent(player1, "Otter");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShortBow()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature spell does not trigger the Otter's prowess")
    void creatureDoesNotTriggerProwess() {
        Permanent ral = addReadyRal(4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent otter = findPermanent(player1, "Otter");
        harness.setHand(player1, List.of(new ShrikeForce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ultimate draws three even when Ral dies, and the emblem copies instants but not artifacts")
    void ultimateSurvivesSourceAndCopiesInstants() {
        Permanent ral = addReadyRal(10);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShrikeForce());
        Card first = new ShortBow();
        Card second = new ShoreUp();
        Card third = new ShortBow();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ral);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("The emblem does not give storm to creatures or opponents' spells")
    void emblemDoesNotCopyCreatureOrOpponentSpell() {
        addReadyRal(10);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ShrikeForce());
        harness.setLibrary(player1, List.of(new ShortBow(), new ShortBow(), new ShortBow()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ShrikeForce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.setHand(player2, List.of(new ShoreUp()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, opponentCreature.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    private Permanent addReadyRal(int loyalty) {
        Permanent ral = harness.addToBattlefieldAndReturn(player1, new RalCracklingWit());
        ral.setCounterCount(CounterType.LOYALTY, loyalty);
        ral.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ral;
    }
}
