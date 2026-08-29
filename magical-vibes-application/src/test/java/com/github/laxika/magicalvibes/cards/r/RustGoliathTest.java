package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RustGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast uses the alternate characteristics and keeps reach and trample")
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new RustGoliath()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent goliath = findPermanent(player1, "Rust Goliath");
        assertThat(gqs.getEffectivePower(gd, goliath)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goliath)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, goliath)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, goliath, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, goliath, Keyword.TRAMPLE)).isTrue();
    }
}
