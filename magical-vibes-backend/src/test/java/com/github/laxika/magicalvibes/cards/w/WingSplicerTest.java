package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WingSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Wing Splicer has ETB token effect and static flying grant")
    void hasCorrectEffects() {
        WingSplicer card = new WingSplicer();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Phyrexian Golem");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(3);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.setHand(player1, List.of(new WingSplicer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Wing Splicer + Golem token

        Permanent golemToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golemToken.getEffectivePower()).isEqualTo(3);
        assertThat(golemToken.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Golem token has flying from Wing Splicer's static ability")
    void golemTokenHasFlying() {
        harness.setHand(player1, List.of(new WingSplicer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wing Splicer itself does not have flying (not a Golem)")
    void wingSplicerDoesNotHaveFlying() {
        harness.addToBattlefield(player1, new WingSplicer());

        Permanent wingSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wing Splicer"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, wingSplicer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is granted to all Golems you control, not just the token")
    void grantsFlyingToOtherGolems() {
        harness.addToBattlefield(player1, new WingSplicer());

        harness.setHand(player1, List.of(new WingSplicer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> golems = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .toList();

        assertThat(golems).isNotEmpty();
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Golems do not get flying")
    void opponentGolemsDoNotGetFlying() {
        harness.addToBattlefield(player1, new WingSplicer());

        harness.setHand(player2, List.of(new WingSplicer()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent p2WingSplicer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wing Splicer"))
                .findFirst()
                .orElseThrow();

        // Player 2's Wing Splicer should not get flying from Player 1's Wing Splicer
        assertThat(gqs.hasKeyword(gd, p2WingSplicer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is lost when Wing Splicer leaves the battlefield")
    void flyingLostWhenWingSplicerLeaves() {
        harness.setHand(player1, List.of(new WingSplicer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        // Verify golem has flying
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FLYING)).isTrue();

        // Remove Wing Splicer from battlefield
        Permanent wingSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wing Splicer"))
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(wingSplicer);

        // Golem should no longer have flying
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FLYING)).isFalse();
    }
}
