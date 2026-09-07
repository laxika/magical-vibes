package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RibbonsOfNight.class, CrawWurm.class, Forest.class})
class RibbonsOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a creature and gains 4 life")
    void dealsDamageAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Draws a card when blue mana was spent")
    void drawsWhenBlueManaWasSpent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setLife(player1, 15);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Does not draw a card when blue mana was not spent")
    void doesNotDrawWithoutBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
