package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YavimayaSapherdTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Yavimaya Sapherd has one ON_ENTER_BATTLEFIELD token creation effect")
    void hasCorrectEffect() {
        YavimayaSapherd card = new YavimayaSapherd();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);

        CreateCreatureTokenEffect effect = (CreateCreatureTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0);
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Saproling");
        assertThat(effect.power()).isEqualTo(1);
        assertThat(effect.toughness()).isEqualTo(1);
        assertThat(effect.color()).isEqualTo(CardColor.GREEN);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(effect.keywords()).isEmpty();
        assertThat(effect.additionalTypes()).isEmpty();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Yavimaya Sapherd puts it on the battlefield")
    void castingPutsOnBattlefieldAndTriggersEtb() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Yavimaya Sapherd"));
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("When Yavimaya Sapherd enters the battlefield, a Saproling token is created")
    void etbCreatesToken() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        GameData gd = harness.getGameData();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("ETB token is a 1/1 green Saproling creature token")
    void tokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        GameData gd = harness.getGameData();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).isEmpty();
    }
}
