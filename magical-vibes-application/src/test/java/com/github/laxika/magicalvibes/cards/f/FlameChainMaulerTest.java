package com.github.laxika.magicalvibes.cards.f;

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
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({FlameChainMauler.class})
class FlameChainMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 and menace until end of turn")
    void resolvingAbilityBoostsAndGrantsMenace() {
        Permanent mauler = addCreatureReady(player1, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent mauler = addCreatureReady(player1, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Activating the ability does not tap Flame-Chain Mauler")
    void activatingDoesNotTap() {
        Permanent mauler = addCreatureReady(player1, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mauler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability requires one red and one generic mana")
    void requiresRedAndGenericMana() {
        addCreatureReady(player1, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedActivationsStackOnlyOnTheirSource() {
        Permanent mauler = addCreatureReady(player1, new FlameChainMauler());
        Permanent other = addCreatureReady(player1, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new FlameChainMauler());
        mauler.setSummoningSick(true);
        mauler.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mauler, Keyword.MENACE)).isTrue();
        assertThat(mauler.isTapped()).isTrue();
    }

    @Test
    void cannotPayWithOnlyGenericMana() {
        addCreatureReady(player1, new FlameChainMauler());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedMenacePreventsSingleBlocker() {
        addCreatureReady(player1, new FlameChainMauler());
        addCreatureReady(player2, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedMenaceAllowsTwoBlockers() {
        addCreatureReady(player1, new FlameChainMauler());
        addCreatureReady(player2, new FlameChainMauler());
        addCreatureReady(player2, new FlameChainMauler());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}
