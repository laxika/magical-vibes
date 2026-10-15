package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoliathSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RibbonsOfNight.class, GoliathSpider.class, Forest.class})
class RibbonsOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a creature and gains 4 life")
    void dealsDamageAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Draws a card when blue mana was spent")
    void drawsWhenBlueManaWasSpent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        harness.setLife(player1, 15);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Does not draw a card when blue mana was not spent")
    void doesNotDrawWithoutBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for blue mana left unspent in the mana pool")
    void doesNotDrawForUnspentBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Fizzles without damage, life gain, or card draw if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        harness.setLife(player1, 15);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("A single blue mana spent draws exactly one card")
    void drawsWithExactlyOneBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        harness.setLife(player1, 15);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can damage your own creature while gaining life and drawing")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathSpider());
        harness.setLife(player1, 15);
        harness.setLife(player2, 12);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Lethal damage still gains four life and draws a card")
    void lethalDamageStillGainsLifeAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathSpider());
        target.setMarkedDamage(2);
        harness.setLife(player1, 15);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RibbonsOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Goliath Spider");
        harness.assertInGraveyard(player2, "Goliath Spider");
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
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
