package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WurmcoilEngineTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Wurmcoil Engine has two ON_DEATH token creation effects")
    void hasCorrectEffects() {
        WurmcoilEngine card = new WurmcoilEngine();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(2);

        CreateTokenEffect deathtouch = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEATH).get(0);
        assertThat(deathtouch.amount()).isEqualTo(1);
        assertThat(deathtouch.tokenName()).isEqualTo("Phyrexian Wurm");
        assertThat(deathtouch.power()).isEqualTo(3);
        assertThat(deathtouch.toughness()).isEqualTo(3);
        assertThat(deathtouch.color()).isNull();
        assertThat(deathtouch.subtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.WURM);
        assertThat(deathtouch.keywords()).containsExactly(Keyword.DEATHTOUCH);
        assertThat(deathtouch.additionalTypes()).containsExactly(CardType.ARTIFACT);

        CreateTokenEffect lifelink = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEATH).get(1);
        assertThat(lifelink.amount()).isEqualTo(1);
        assertThat(lifelink.tokenName()).isEqualTo("Phyrexian Wurm");
        assertThat(lifelink.power()).isEqualTo(3);
        assertThat(lifelink.toughness()).isEqualTo(3);
        assertThat(lifelink.color()).isNull();
        assertThat(lifelink.subtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.WURM);
        assertThat(lifelink.keywords()).containsExactly(Keyword.LIFELINK);
        assertThat(lifelink.additionalTypes()).containsExactly(CardType.ARTIFACT);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Wurmcoil Engine puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wurmcoil Engine"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Wurmcoil Engine dies, two Phyrexian Wurm tokens are created")
    void deathTriggerCreatesTwoTokens() {
        harness.addToBattlefield(player1, new WurmcoilEngine());

        // Use Wrath of God to kill Wurmcoil Engine
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — Wurmcoil Engine dies

        GameData gd = harness.getGameData();

        // Wurmcoil Engine should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wurmcoil Engine"));

        // Two death triggers should be on the stack
        assertThat(gd.stack).hasSize(2);

        // Resolve both death triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Two Phyrexian Wurm tokens should be on the battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Wurm"))
                .toList();
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("Death trigger tokens are 3/3 colorless Phyrexian Wurm artifact creatures")
    void tokensHaveCorrectProperties() {
        harness.addToBattlefield(player1, new WurmcoilEngine());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath
        harness.passBothPriorities(); // Resolve first death trigger
        harness.passBothPriorities(); // Resolve second death trigger

        GameData gd = harness.getGameData();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Wurm"))
                .toList();

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.PHYREXIAN, CardSubtype.WURM);
            assertThat(token.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("One death token has deathtouch and the other has lifelink")
    void tokensHaveCorrectKeywords() {
        harness.addToBattlefield(player1, new WurmcoilEngine());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath
        harness.passBothPriorities(); // Resolve first death trigger
        harness.passBothPriorities(); // Resolve second death trigger

        GameData gd = harness.getGameData();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Wurm"))
                .toList();
        assertThat(tokens).hasSize(2);

        // One token should have deathtouch, the other lifelink
        assertThat(tokens).anyMatch(p -> p.getCard().getKeywords().contains(Keyword.DEATHTOUCH));
        assertThat(tokens).anyMatch(p -> p.getCard().getKeywords().contains(Keyword.LIFELINK));

        // Each token should have exactly one of the two keywords
        Permanent deathtouchToken = tokens.stream()
                .filter(p -> p.getCard().getKeywords().contains(Keyword.DEATHTOUCH))
                .findFirst().orElseThrow();
        assertThat(deathtouchToken.getCard().getKeywords()).doesNotContain(Keyword.LIFELINK);

        Permanent lifelinkToken = tokens.stream()
                .filter(p -> p.getCard().getKeywords().contains(Keyword.LIFELINK))
                .findFirst().orElseThrow();
        assertThat(lifelinkToken.getCard().getKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }
}
