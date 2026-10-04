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
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(4);
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

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
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
        target.setTapped(true);
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
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

        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(4);
    }
}
