package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SabertoothOutrider.class, ColossodonYearling.class, SarkhansRage.class})
class SabertoothOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains first strike when it attacks with 8 total power")
    void gainsFirstStrikeWithEnoughTotalPower() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when creatures you control have less than 8 total power")
    void doesNotTriggerWithInsufficientTotalPower() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new ColossodonYearling());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Formidable is checked again when the attack trigger resolves")
    void doesNotGainFirstStrikeWhenTotalPowerFallsBeforeResolution() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        Permanent support = addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());
        harness.setHand(player2, List.of(new SarkhansRage()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, support.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(support);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opposing creatures do not count toward formidable")
    void doesNotCountOpponentsPower() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player2, new SabertoothOutrider());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Only the attacking Outrider gains first strike, even above the threshold")
    void grantsFirstStrikeOnlyToAttackingOutrider() {
        Permanent attacker = addCreatureReady(player1, new SabertoothOutrider());
        Permanent nonattacker = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new ColossodonYearling());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Formidable lets Outrider kill its blocker first and trample over")
    void firstStrikeAndTrampleWorkTogetherInCombat() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new SabertoothOutrider());
        Permanent blocker = addCreatureReady(player2, new SabertoothOutrider());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(outrider);
    }
}
