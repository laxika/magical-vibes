package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedReversal;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenGibbons.class, BlessedReversal.class, GiantCockroach.class})
class HiddenGibbonsTest extends BaseCardTest {

    private Permanent addHiddenGibbons() {
        return harness.addToBattlefieldAndReturn(player1, new HiddenGibbons());
    }

    private void prepareCast(Player castingPlayer) {
        harness.forceActivePlayer(castingPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("An opponent's instant makes Hidden Gibbons a 4/4 Ape creature")
    void becomesApeCreatureWhenOpponentCastsInstant() {
        Permanent gibbons = addHiddenGibbons();
        prepareCast(player2);

        harness.castFromHand(player2, new BlessedReversal(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gibbons)).isTrue();
        assertThat(gqs.isEnchantment(gd, gibbons)).isFalse();
        assertThat(gqs.getEffectivePower(gd, gibbons)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gibbons)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, gibbons)).containsExactly(CardSubtype.APE);
    }

    @Test
    @DisplayName("A non-instant spell does not trigger Hidden Gibbons")
    void doesNotTriggerForNonInstantSpell() {
        Permanent gibbons = addHiddenGibbons();
        prepareCast(player2);

        harness.castFromHand(player2, new GiantCockroach(), "{3}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, gibbons)).isTrue();
        assertThat(gqs.isCreature(gd, gibbons)).isFalse();
    }

    @Test
    @DisplayName("Hidden Gibbons does not trigger after becoming a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent gibbons = addHiddenGibbons();
        prepareCast(player2);

        harness.castFromHand(player2, new BlessedReversal(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player2, new BlessedReversal(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, gibbons)).isTrue();
        assertThat(gqs.isEnchantment(gd, gibbons)).isFalse();
    }

    @Test
    void doesNotTriggerForControllerCast() {
        Permanent gibbons = addHiddenGibbons();
        prepareCast(player1);

        harness.castFromHand(player1, new BlessedReversal(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, gibbons)).isTrue();
        assertThat(gqs.isCreature(gd, gibbons)).isFalse();
    }
}
