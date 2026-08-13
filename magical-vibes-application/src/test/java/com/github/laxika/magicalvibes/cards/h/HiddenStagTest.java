package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenStagTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's land play makes Hidden Stag a 3/2 Elk Beast creature")
    void becomesCreatureWhenOpponentPlaysLand() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenStag)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hiddenStag)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenStag))
                .containsExactly(CardSubtype.ELK, CardSubtype.BEAST);
    }

    @Test
    @DisplayName("A controller's land play restores Hidden Stag as an enchantment")
    void becomesEnchantmentWhenControllerPlaysLand() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        prepareLandPlay(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenStag)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenStag)).isFalse();
    }

    @Test
    @DisplayName("A controller's land play does not trigger Hidden Stag while it is an enchantment")
    void doesNotTriggerControllerLandPlayWhileEnchantment() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenStag)).isFalse();
    }

    private void prepareLandPlay(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
