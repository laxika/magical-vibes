package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HawkeaterMothTest extends BaseCardTest {

    @Test
    @DisplayName("Hawkeater Moth has flying and shroud")
    void hasFlyingAndShroud() {
        Permanent moth = harness.addToBattlefieldAndReturn(player1, new HawkeaterMoth());

        assertThat(gqs.hasKeyword(gd, moth, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, moth, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Hawkeater Moth cannot be targeted by a spell")
    void cannotBeTargetedBySpell() {
        harness.addToBattlefield(player1, new HawkeaterMoth());
        Permanent moth = findPermanent(player1, "Hawkeater Moth");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, moth.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
