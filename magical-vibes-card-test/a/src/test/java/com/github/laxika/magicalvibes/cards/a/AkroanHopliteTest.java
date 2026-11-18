package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkroanHoplite.class, LightningStrike.class})
class AkroanHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 for the number of attacking creatures you control")
    void boostsForAttackingCreaturesYouControl() {
        Permanent hoplite = addCreatureReady(player1, new AkroanHoplite());
        addCreatureReady(player1, createCreatureCard("Ally"));
        addCreatureReady(player2, createCreatureCard("Opponent"));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, hoplite)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, hoplite)).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hoplite = addCreatureReady(player1, new AkroanHoplite());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, hoplite)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, hoplite)).isZero();
    }

    @Test
    @DisplayName("Each attacking Hoplite counts itself and the other attackers, but not nonattackers")
    void eachAttackingHopliteCountsOnlyAttackers() {
        Permanent first = addCreatureReady(player1, new AkroanHoplite());
        Permanent second = addCreatureReady(player1, new AkroanHoplite());
        Permanent nonattacker = addCreatureReady(player1, new AkroanHoplite());
        addCreatureReady(player2, new AkroanHoplite());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
    }

    @Test
    @DisplayName("Counts attacking creatures when the ability resolves, after another attacker dies")
    void countsAttackersAtResolution() {
        Permanent survivor = addCreatureReady(player1, new AkroanHoplite());
        Permanent removed = addCreatureReady(player1, new AkroanHoplite());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        harness.castInstant(player2, 0, removed.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor).doesNotContain(removed);
        assertThat(PermanentGrants.powerBoostOf(gd, survivor)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, survivor)).isZero();
    }

    @Test
    @DisplayName("A Hoplite that dies before its attack trigger resolves cannot boost another Hoplite")
    void removedSourceDoesNotBoostNonattackingHoplite() {
        Permanent attacker = addCreatureReady(player1, new AkroanHoplite());
        Permanent nonattacker = addCreatureReady(player1, new AkroanHoplite());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonattacker).doesNotContain(attacker);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattacker)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private com.github.laxika.magicalvibes.model.Card createCreatureCard(String name) {
        com.github.laxika.magicalvibes.model.Card card = new com.github.laxika.magicalvibes.model.Card() {};
        card.setName(name);
        card.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
