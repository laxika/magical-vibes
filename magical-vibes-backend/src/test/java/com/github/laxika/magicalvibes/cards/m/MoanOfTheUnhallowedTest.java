package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoanOfTheUnhallowedTest extends BaseCardTest {

    @Test
    @DisplayName("Moan of the Unhallowed has correct effects and flashback cost")
    void hasCorrectProperties() {
        MoanOfTheUnhallowed card = new MoanOfTheUnhallowed();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect effect = (CreateCreatureTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
        assertThat(effect.tapped()).isFalse();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{5}{B}{B}");
    }

    @Test
    @DisplayName("Casting Moan of the Unhallowed creates two 2/2 Zombie tokens")
    void createsTwoZombies() {
        harness.setHand(player1, List.of(new MoanOfTheUnhallowed()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombies).hasSize(2);

        for (Permanent zombie : zombies) {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving (normal cast)")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new MoanOfTheUnhallowed()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Moan of the Unhallowed"));
    }

    @Test
    @DisplayName("Flashback creates two 2/2 Zombie tokens")
    void flashbackCreatesTwoZombies() {
        harness.setGraveyard(player1, List.of(new MoanOfTheUnhallowed()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombies).hasSize(2);

        for (Permanent zombie : zombies) {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new MoanOfTheUnhallowed()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Moan of the Unhallowed"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Moan of the Unhallowed"));
    }
}
