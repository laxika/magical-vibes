package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractPaintmageTest extends BaseCardTest {

    @Test
    @DisplayName("Has two precombat main AwardRestrictedManaEffects for instants and sorceries")
    void hasPrecombatMainRestrictedManaEffects() {
        AbstractPaintmage card = new AbstractPaintmage();

        assertThat(card.getEffects(EffectSlot.PRECOMBAT_MAIN_TRIGGERED)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.PRECOMBAT_MAIN_TRIGGERED))
                .allMatch(AwardRestrictedManaEffect.class::isInstance);

        AwardRestrictedManaEffect blue = (AwardRestrictedManaEffect) card.getEffects(EffectSlot.PRECOMBAT_MAIN_TRIGGERED).get(0);
        AwardRestrictedManaEffect red = (AwardRestrictedManaEffect) card.getEffects(EffectSlot.PRECOMBAT_MAIN_TRIGGERED).get(1);
        assertThat(blue.color()).isEqualTo(ManaColor.BLUE);
        assertThat(blue.amount()).isEqualTo(1);
        assertThat(red.color()).isEqualTo(ManaColor.RED);
        assertThat(red.amount()).isEqualTo(1);
        assertThat(blue.allowedSpellTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);
        assertThat(red.allowedSpellTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);
    }

    @Test
    @DisplayName("Precombat main trigger adds one instant/sorcery-only blue and red mana")
    void precombatMainTriggerAddsRestrictedMana() {
        addReadyPaintmage(player1);

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).hasSize(2);

        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Restricted mana can pay for an instant spell")
    void restrictedManaPaysForInstant() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, findPermanent(player1, "Abstract Paintmage").getId());
        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(0);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a creature spell")
    void restrictedManaCannotPayForCreature() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on opponent's precombat main phase")
    void doesNotTriggerOnOpponentsTurn() {
        addReadyPaintmage(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersOnLaterTurn() {
        addReadyPaintmage(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyPaintmage(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new AbstractPaintmage());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
