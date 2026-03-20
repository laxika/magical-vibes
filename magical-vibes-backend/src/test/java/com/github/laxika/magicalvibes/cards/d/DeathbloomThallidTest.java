package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathbloomThallidTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Deathbloom Thallid has one ON_DEATH token creation effect")
    void hasCorrectEffect() {
        DeathbloomThallid card = new DeathbloomThallid();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);

        CreateCreatureTokenEffect effect = (CreateCreatureTokenEffect) card.getEffects(EffectSlot.ON_DEATH).get(0);
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
    @DisplayName("Casting Deathbloom Thallid puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new DeathbloomThallid()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Deathbloom Thallid"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Deathbloom Thallid dies, a Saproling token is created")
    void deathTriggerCreatesToken() {
        harness.addToBattlefield(player1, new DeathbloomThallid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — Deathbloom Thallid dies

        GameData gd = harness.getGameData();

        // Deathbloom Thallid should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deathbloom Thallid"));

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Saproling token should be on the battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Death trigger token is a 1/1 green Saproling creature")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new DeathbloomThallid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath
        harness.passBothPriorities(); // Resolve death trigger

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
