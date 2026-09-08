package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChimericEggTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a nonartifact spell puts a charge counter on Chimeric Egg")
    void opponentNonartifactSpellAddsChargeCounter() {
        Permanent egg = addReadyEgg();
        prepareOpponentSpell(new GrizzlyBears(), ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent casting an artifact spell does not trigger Chimeric Egg")
    void opponentArtifactSpellDoesNotAddChargeCounter() {
        Permanent egg = addReadyEgg();
        prepareOpponentSpell(new EtheriumSculptor(), ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing three charge counters animates Chimeric Egg until end of turn")
    void activatesAnimation() {
        Permanent egg = addReadyEgg();
        egg.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.isArtifact(egg)).isTrue();
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(6);
        assertThat(egg.getTransientSubtypes()).contains(CardSubtype.CONSTRUCT);
        assertThat(egg.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, egg)).isFalse();
        assertThat(egg.getTransientSubtypes()).doesNotContain(CardSubtype.CONSTRUCT);
        assertThat(egg.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    void cannotActivateWithFewerThanThreeChargeCounters() {
        addReadyEgg().setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEgg() {
        Permanent egg = new Permanent(new ChimericEgg());
        egg.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(egg);
        return egg;
    }

    private void prepareOpponentSpell(com.github.laxika.magicalvibes.model.Card spell,
                                      ManaColor color, int manaValue) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, manaValue);
    }
}
