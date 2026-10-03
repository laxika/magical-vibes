package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaxossTorment.class, GloriousAnthem.class, GrizzlyBears.class})
class DaxossTormentTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry animates Daxos's Torment")
    void ownEntryAnimatesIt() {
        Permanent torment = castDaxossTorment();

        assertThat(gqs.isCreature(gd, torment)).isTrue();
        assertThat(gqs.isEnchantment(gd, torment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, torment)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, torment)).isEqualTo(5);
        assertThat(torment.getTransientSubtypes()).contains(CardSubtype.DEMON);
        assertThat(gqs.hasKeyword(gd, torment, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, torment, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control animates it")
    void anotherEnchantmentEntryAnimatesIt() {
        Permanent torment = harness.addToBattlefieldAndReturn(player1, new DaxossTorment());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, torment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, torment)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, torment)).isEqualTo(6);
    }

    @Test
    @DisplayName("Non-enchantments and opponents' enchantments do not trigger it")
    void ignoresNonEnchantmentAndOpponentEnchantment() {
        Permanent torment = harness.addToBattlefieldAndReturn(player1, new DaxossTorment());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, torment)).isFalse();

        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, torment)).isFalse();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent torment = castDaxossTorment();
        assertThat(gqs.isCreature(gd, torment)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, torment)).isFalse();
        assertThat(gqs.isEnchantment(gd, torment)).isTrue();
        assertThat(torment.getTransientSubtypes()).isEmpty();
    }

    private Permanent castDaxossTorment() {
        DaxossTorment card = new DaxossTorment();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Daxos's Torment");
    }

    @Test
    @DisplayName("Its own entry triggers once and animation waits for resolution")
    void ownEntryAnimationUsesTheStack() {
        harness.setHand(player1, List.of(new DaxossTorment()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent torment = findPermanent(player1, "Daxos's Torment");
        assertThat(gqs.isCreature(gd, torment)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, torment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, torment)).isEqualTo(5);
    }

    @Test
    @DisplayName("A second Torment animates itself and the first Torment")
    void secondTormentAnimatesBothEnchantments() {
        castDaxossTorment();
        harness.setHand(player1, List.of(new DaxossTorment()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Daxos's Torment")).hasSize(2)
                .allSatisfy(torment -> {
                    assertThat(gqs.isCreature(gd, torment)).isTrue();
                    assertThat(gqs.isEnchantment(gd, torment)).isTrue();
                    assertThat(gqs.getEffectivePower(gd, torment)).isEqualTo(5);
                    assertThat(gqs.getEffectiveToughness(gd, torment)).isEqualTo(5);
                    assertThat(gqs.hasKeyword(gd, torment, Keyword.FLYING)).isTrue();
                    assertThat(gqs.hasKeyword(gd, torment, Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("Haste lets Torment attack on the turn it enters")
    void canAttackImmediately() {
        castDaxossTorment();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("After animation expires, another enchantment can animate it again")
    void canAnimateAgainAfterCleanup() {
        Permanent torment = castDaxossTorment();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, torment)).isFalse();
        assertThat(gqs.hasKeyword(gd, torment, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, torment, Keyword.HASTE)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, torment)).isTrue();
        assertThat(gqs.isEnchantment(gd, torment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, torment)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, torment)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, torment, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, torment, Keyword.HASTE)).isTrue();
    }
}
