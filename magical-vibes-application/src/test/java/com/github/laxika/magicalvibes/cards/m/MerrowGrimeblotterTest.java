package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerrowGrimeblotterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives target creature -2/-0 and untaps the source")
    void weakensTargetAndUntaps() {
        Permanent merrow = addTapped(player1, new MerrowGrimeblotter());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isZero();
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(2);
        // Paying {Q} untapped the source.
        assertThat(merrow.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The -2/-0 wears off at end of turn")
    void wearsOffAtCleanup() {
        addTapped(player1, new MerrowGrimeblotter());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new MerrowGrimeblotter());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTapped(player1, new MerrowGrimeblotter());
        Permanent enchantment = addReady(player2, new Pacifism());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
