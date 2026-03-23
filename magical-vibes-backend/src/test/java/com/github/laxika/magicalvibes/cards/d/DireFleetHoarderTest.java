package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DireFleetHoarderTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one ON_DEATH effect that creates a Treasure token")
    void hasCorrectEffect() {
        DireFleetHoarder card = new DireFleetHoarder();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEATH).get(0);
        assertThat(effect.tokenName()).isEqualTo("Treasure");
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.TREASURE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Dire Fleet Hoarder puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new DireFleetHoarder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dire Fleet Hoarder"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Dire Fleet Hoarder dies, a Treasure token is created")
    void deathTriggerCreatesTreasureToken() {
        harness.addToBattlefield(player1, new DireFleetHoarder());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — Dire Fleet Hoarder dies

        GameData gd = harness.getGameData();

        // Dire Fleet Hoarder should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dire Fleet Hoarder"));

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Treasure token should be on the battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Death trigger token is a Treasure artifact with correct properties")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new DireFleetHoarder());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath
        harness.passBothPriorities(); // Resolve death trigger

        GameData gd = harness.getGameData();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(token.getCard().isToken()).isTrue();
    }
}
