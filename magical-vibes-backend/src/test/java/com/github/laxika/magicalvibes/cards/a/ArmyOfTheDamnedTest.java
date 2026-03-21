package com.github.laxika.magicalvibes.cards.a;

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

class ArmyOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("Army of the Damned has correct effects and flashback cost")
    void hasCorrectProperties() {
        ArmyOfTheDamned card = new ArmyOfTheDamned();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect effect = (CreateCreatureTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(13);
        assertThat(effect.tapped()).isTrue();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{7}{B}{B}{B}");
    }

    @Test
    @DisplayName("Casting Army of the Damned creates thirteen tapped 2/2 Zombie tokens")
    void createsThirteenTappedZombies() {
        harness.setHand(player1, List.of(new ArmyOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombies).hasSize(13);

        for (Permanent zombie : zombies) {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving (normal cast)")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new ArmyOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Army of the Damned"));
    }

    @Test
    @DisplayName("Flashback creates thirteen tapped Zombie tokens")
    void flashbackCreatesThirteenTappedZombies() {
        harness.setGraveyard(player1, List.of(new ArmyOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombies).hasSize(13);

        for (Permanent zombie : zombies) {
            assertThat(zombie.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new ArmyOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Army of the Damned"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Army of the Damned"));
    }
}
