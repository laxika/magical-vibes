package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiegeOfTowers.class, Mountain.class, Forest.class})
class SiegeOfTowersTest extends BaseCardTest {

    @Test
    @DisplayName("Permanently makes a target Mountain a 3/1 creature that is still a land")
    void animatesMountainPermanently() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SiegeOfTowers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
        assertThat(mountain.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Replicate makes one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castSiegeOfTowers(mountain, List.of("{1}{R}", "{1}{R}"));

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Mountain permanent")
    void cannotTargetNonMountain() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SiegeOfTowers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountain");
    }

    private void castSiegeOfTowers(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new SiegeOfTowers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.RED, 1 + replicatePayments.size());
        harness.castSorceryWithRepeatedCosts(player1, 0, replicatePayments, List.of(target.getId()));
    }
}
