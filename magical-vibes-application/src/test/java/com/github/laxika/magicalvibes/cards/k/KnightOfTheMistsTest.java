package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfTheMists.class, KnightOfValor.class, LongbowArcher.class})
class KnightOfTheMistsTest extends BaseCardTest {

    private void castKnightOfTheMists() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new KnightOfTheMists(), "{2}{U}");
        harness.passBothPriorities(); // resolve creature — ETB target prompt
    }

    @Test
    @DisplayName("ETB targets Knights, including itself, but not other creatures")
    void etbTargetingIncludesSelfAndExcludesNonKnights() {
        harness.addToBattlefield(player2, new LongbowArcher());
        UUID nonKnightId = harness.getPermanentId(player2, "Longbow Archer");

        castKnightOfTheMists();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID selfId = harness.getPermanentId(player1, "Knight of the Mists");
        assertThat(choice.validIds()).contains(selfId).doesNotContain(nonKnightId);
    }

    @Test
    @DisplayName("Flanking weakens a non-flanking blocker until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new KnightOfTheMists());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LongbowArcher());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {U} leaves the targeted Knight on the battlefield")
    void payingSavesTheTargetedKnight() {
        harness.addToBattlefield(player2, new KnightOfValor());
        UUID opponentKnightId = harness.getPermanentId(player2, "Knight of Valor");

        castKnightOfTheMists();
        harness.handlePermanentChosen(player1, opponentKnightId);
        harness.passBothPriorities(); // resolve ETB → may-pay prompt

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Knight of Valor");
        harness.assertOnBattlefield(player1, "Knight of the Mists");
    }

    @Test
    @DisplayName("Declining payment destroys the targeted Knight; can't be regenerated")
    void decliningDestroysTargetedKnight() {
        harness.addToBattlefield(player2, new KnightOfValor());
        UUID opponentKnightId = harness.getPermanentId(player2, "Knight of Valor");

        castKnightOfTheMists();
        harness.handlePermanentChosen(player1, opponentKnightId);
        harness.passBothPriorities(); // resolve ETB → may-pay prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Knight of Valor");
        harness.assertOnBattlefield(player1, "Knight of the Mists");
    }

    @Test
    @DisplayName("With no other Knights, declining destroys itself")
    void decliningWithOnlySelfDestroysSelf() {
        castKnightOfTheMists();
        UUID selfId = harness.getPermanentId(player1, "Knight of the Mists");
        harness.handlePermanentChosen(player1, selfId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Knight of the Mists");
    }

    @Test
    @DisplayName("Accepting without mana falls through to destroy")
    void acceptingWithoutManaDestroys() {
        harness.addToBattlefield(player2, new KnightOfValor());
        UUID opponentKnightId = harness.getPermanentId(player2, "Knight of Valor");

        castKnightOfTheMists();
        harness.handlePermanentChosen(player1, opponentKnightId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // no {U} in pool

        harness.assertNotOnBattlefield(player2, "Knight of Valor");
    }
}
