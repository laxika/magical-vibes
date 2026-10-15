package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BlindingMage;
import com.github.laxika.magicalvibes.cards.s.SpiderSlayerHatredHoned;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LizardConnorssCurse.class, BlindingMage.class, SpiderSlayerHatredHoned.class})
class LizardConnorssCurseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB permanently turns another creature into a green 4/4 Lizard without abilities")
    void transformsAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindingMage());
        castCurse(target);

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.LIZARD);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.LIZARD);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB may choose no target")
    void mayChooseNoTarget() {
        harness.castFromHand(player1, new LizardConnorssCurse(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(5);
    }

    @Test
    @DisplayName("Transformation replaces an artifact creature's other card types")
    void replacesOtherCardTypes() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpiderSlayerHatredHoned());

        castCurse(target);

        assertThat(gqs.getEffectiveCardTypes(gd, target)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.LIZARD);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another Lizard can be targeted and loses trample while retaining counters")
    void removesKeywordsAndKeepsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LizardConnorssCurse());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castCurse(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Transformation persists after its source leaves the battlefield")
    void persistsAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindingMage());
        castCurse(target);
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.LIZARD);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves before resolution is not transformed in the graveyard")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindingMage());
        harness.setHand(player1, List.of(new LizardConnorssCurse()));
        addManaForCurse();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Blinding Mage");
        Permanent returned = harness.addToBattlefieldAndReturn(player2,
                gd.playerGraveyards.get(player2.getId()).removeFirst());
        assertThat(gqs.getEffectiveColors(gd, returned)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.WIZARD);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    private void castCurse(Permanent target) {
        harness.setHand(player1, List.of(new LizardConnorssCurse()));
        addManaForCurse();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForCurse() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
