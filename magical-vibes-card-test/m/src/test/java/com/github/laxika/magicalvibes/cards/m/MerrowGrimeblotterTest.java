package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerrowGrimeblotter.class, GrizzlyBears.class, Pacifism.class})
class MerrowGrimeblotterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives target creature -2/-0 and untaps the source")
    void weakensTargetAndUntaps() {
        Permanent merrow = addTapped(player1, new MerrowGrimeblotter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        addCreatureReady(player1, new MerrowGrimeblotter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent enchantment = addCreatureReady(player2, new Pacifism());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        enchantment.setAttachedTo(enchantedCreature.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Black mana pays the hybrid cost and the source can target itself")
    void blackManaAndSelfTarget() {
        Permanent merrow = addTapped(player1, new MerrowGrimeblotter());
        harness.addMana(player1, ManaColor.BLACK, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, merrow.getId());

        assertThat(merrow.isTapped()).isFalse();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, merrow)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, merrow)).isZero();
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, merrow)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness cannot pay the untap cost")
    void summoningSicknessPreventsActivation() {
        Permanent merrow = addTapped(player1, new MerrowGrimeblotter());
        merrow.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, merrow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(merrow.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations stack and can reduce power below zero")
    void repeatedActivationsStack() {
        Permanent merrow = addTapped(player1, new MerrowGrimeblotter());
        Permanent target = addCreatureReady(player2, new MerrowGrimeblotter());
        harness.addMana(player1, ManaColor.BLUE, 4);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        merrow.tap();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(-2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
