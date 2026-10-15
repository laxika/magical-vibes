package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KozileksShrieker.class})
class KozileksShriekerTest extends BaseCardTest {

    @Test
    @DisplayName("Kozilek's Shrieker gets +1/+0 and menace after paying its colorless activation cost")
    void boostsAndGainsMenace() {
        Permanent shrieker = addCreatureReady(player1, new KozileksShrieker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shrieker.getEffectivePower()).isEqualTo(4);
        assertThat(shrieker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shrieker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Kozilek's Shrieker's boost and menace wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent shrieker = addCreatureReady(player1, new KozileksShrieker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrieker.getEffectivePower()).isEqualTo(3);
        assertThat(shrieker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shrieker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Kozilek's Shrieker requires colorless mana for its activation")
    void requiresColorlessMana() {
        Permanent shrieker = addCreatureReady(player1, new KozileksShrieker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(shrieker.getEffectivePower()).isEqualTo(3);
        assertThat(shrieker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shrieker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack only on the activating Shrieker")
    void repeatedActivationsStackOnSourceOnly() {
        Permanent shrieker = addCreatureReady(player1, new KozileksShrieker());
        Permanent other = addCreatureReady(player1, new KozileksShrieker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrieker.getEffectivePower()).isEqualTo(5);
        assertThat(shrieker.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shrieker, Keyword.MENACE)).isTrue();
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Shrieker can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent shrieker = harness.addToBattlefieldAndReturn(player1, new KozileksShrieker());
        shrieker.setSummoningSick(true);
        shrieker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shrieker.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, shrieker, Keyword.MENACE)).isTrue();
        assertThat(shrieker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted menace rejects one blocker and allows two")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new KozileksShrieker());
        addCreatureReady(player2, new KozileksShrieker());
        addCreatureReady(player2, new KozileksShrieker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }
}
