package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WayfaringTemple.class, DrudgeBeetle.class, CallOfTheConclave.class})
class WayfaringTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures its controller controls")
    void powerAndToughnessEqualControlledCreatureCount() {
        Permanent temple = addReadyTemple();

        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(1);

        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.addToBattlefield(player2, new DrudgeBeetle());

        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a player triggers populate")
    void combatDamageTriggersPopulate() {
        Permanent temple = addReadyTemple();
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        temple.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
    }

    @Test
    @DisplayName("Populate does nothing when its combat damage is prevented by a blocker")
    void blockedCombatDamageDoesNotTriggerPopulate() {
        Permanent temple = addReadyTemple();
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        temple.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
    }

    @Test
    @DisplayName("Populate cannot copy an opponent's token or a nontoken creature")
    void populateWithoutControlledCreatureTokensCreatesNothing() {
        Permanent temple = addReadyTemple();
        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.addToBattlefield(player2, creatureToken("Soldier Token"));
        temple.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Soldier Token")).isEmpty();
        assertThat(findPermanents(player2, "Soldier Token")).hasSize(1);
    }

    @Test
    @DisplayName("Populate checks for creature tokens when the trigger resolves")
    void tokenLeavingBeforeResolutionCannotBeCopied() {
        Permanent temple = addReadyTemple();
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Soldier Token"));
        temple.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(token);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Token")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(1);
    }

    @Test
    @DisplayName("Populate lets the controller choose among their creature tokens")
    void populateCopiesOnlyChosenToken() {
        Permanent temple = addReadyTemple();
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, creatureToken("Saproling Token"));
        temple.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
        assertThat(findPermanents(player1, "Saproling Token")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(4);
    }

    @Test
    @DisplayName("The characteristic-defining ability works in hand and graveyard")
    void creatureCountDefinesPowerAndToughnessOutsideBattlefield() {
        WayfaringTemple inHand = new WayfaringTemple();
        WayfaringTemple inGraveyard = new WayfaringTemple();
        harness.setHand(player1, java.util.List.of(inHand));
        harness.setGraveyard(player1, java.util.List.of(inGraveyard));
        harness.addToBattlefield(player2, new DrudgeBeetle());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new DrudgeBeetle());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Populate copies a real creature token without its counters or tapped status")
    void populateCopiesTokenCharacteristicsWithoutCountersOrTappedStatus() {
        Permanent temple = addReadyTemple();
        harness.setHand(player1, java.util.List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, java.util.List.of());
        Permanent original = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.tap();
        temple.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        var tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        Permanent copy = tokens.stream().filter(p -> !p.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(3);
    }

    private Permanent addReadyTemple() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new WayfaringTemple());
        temple.setSummoningSick(false);

        return temple;
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
