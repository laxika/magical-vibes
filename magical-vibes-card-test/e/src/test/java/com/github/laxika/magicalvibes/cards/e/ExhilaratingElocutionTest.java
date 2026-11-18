package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExhilaratingElocution.class, SpinedKarok.class, Expel.class})
class ExhilaratingElocutionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two counters on the target and boosts other creatures you control")
    void putsCountersOnTargetAndBoostsOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The temporary boost wears off while counters remain")
    void temporaryBoostWearsOffButCountersRemain() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal sole target prevents the boost to other creatures")
    void removedTargetPreventsOtherCreaturesFromBeingBoosted() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        target.tap();
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Exhilarating Elocution");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreatureDoesNotReceiveBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        Permanent later = harness.enterBattlefieldAndReturn(player1, new SpinedKarok());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(4);
    }
}
