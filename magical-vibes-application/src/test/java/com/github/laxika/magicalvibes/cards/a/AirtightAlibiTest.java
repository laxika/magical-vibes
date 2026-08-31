package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConvenientTarget;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AirtightAlibi.class, ConvenientTarget.class, GrizzlyBears.class})
class AirtightAlibiTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, boosts, grants hexproof, and clears suspicion from the enchanted creature")
    void resolvesEnterTheBattlefieldEffects() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchanted.tap();
        enchanted.setSuspected(true);
        other.setSuspected(true);

        harness.setHand(player1, List.of(new AirtightAlibi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HEXPROOF)).isTrue();
        assertThat(enchanted.isSuspected()).isFalse();
        assertThat(other.isSuspected()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature cannot become suspected while the Aura remains attached")
    void preventsBecomingSuspected() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AirtightAlibi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ConvenientTarget()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.isSuspected()).isFalse();
    }
}
