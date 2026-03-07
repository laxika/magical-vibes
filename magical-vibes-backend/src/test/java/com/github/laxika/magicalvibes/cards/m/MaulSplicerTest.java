package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaulSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Maul Splicer has ETB token effect and static trample grant")
    void hasCorrectEffects() {
        MaulSplicer card = new MaulSplicer();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect tokenEffect =
                (CreateCreatureTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(2);
        assertThat(tokenEffect.tokenName()).isEqualTo("Phyrexian Golem");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(3);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("ETB creates two 3/3 colorless Phyrexian Golem artifact creature tokens")
    void etbCreatesTwoGolemTokens() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Maul Splicer + 2 Golem tokens

        List<Permanent> golemTokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(golemTokens).hasSize(2);

        for (Permanent golem : golemTokens) {
            assertThat(golem.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
            assertThat(golem.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(golem.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(golem.getEffectivePower()).isEqualTo(3);
            assertThat(golem.getEffectiveToughness()).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Golem tokens have trample from Maul Splicer's static ability")
    void golemTokensHaveTrample() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> golemTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();

        assertThat(golemTokens).hasSize(2);
        for (Permanent golem : golemTokens) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Maul Splicer itself does not have trample (not a Golem)")
    void maulSplicerDoesNotHaveTrample() {
        harness.addToBattlefield(player1, new MaulSplicer());

        Permanent maulSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Maul Splicer"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, maulSplicer, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is granted to all Golems you control, not just the tokens")
    void grantsTrampleToOtherGolems() {
        harness.addToBattlefield(player1, new MaulSplicer());

        // Cast a second Maul Splicer to get Golem tokens on the battlefield
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> golems = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .toList();

        assertThat(golems).isNotEmpty();
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Golems do not get trample")
    void opponentGolemsDoNotGetTrample() {
        harness.addToBattlefield(player1, new MaulSplicer());

        // Put a Golem on the opponent's battlefield via a second Maul Splicer
        harness.setHand(player2, List.of(new MaulSplicer()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Player 2's Maul Splicer should not get trample from Player 1's Maul Splicer
        Permanent p2MaulSplicer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Maul Splicer"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, p2MaulSplicer, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is lost when Maul Splicer leaves the battlefield")
    void trampleLostWhenMaulSplicerLeaves() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        // Verify golem has trample
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.TRAMPLE)).isTrue();

        // Remove Maul Splicer from battlefield
        Permanent maulSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Maul Splicer"))
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(maulSplicer);

        // Golem should no longer have trample
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.TRAMPLE)).isFalse();
    }
}
