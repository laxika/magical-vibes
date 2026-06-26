package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReapTheSeagrafTest extends BaseCardTest {

    @Test
    @DisplayName("Has spell token creation effect and flashback cost")
    void hasCorrectEffectsAndFlashbackCost() {
        ReapTheSeagraf card = new ReapTheSeagraf();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Zombie");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.color()).isEqualTo(CardColor.BLACK);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(effect.tapped()).isFalse();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{4}{U}");
    }

    @Test
    @DisplayName("Casting Reap the Seagraf creates one 2/2 black Zombie token")
    void createsOneZombieToken() {
        harness.setHand(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> zombies = zombieTokens();
        assertThat(zombies).hasSize(1);

        Permanent zombie = zombies.getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Normal cast puts Reap the Seagraf into graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reap the Seagraf"));
    }

    @Test
    @DisplayName("Flashback creates one Zombie token")
    void flashbackCreatesOneZombieToken() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(zombieTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Flashback exiles Reap the Seagraf after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Reap the Seagraf"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reap the Seagraf"));
    }

    @Test
    @DisplayName("Flashback puts Reap the Seagraf on the stack as a sorcery")
    void flashbackPutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reap the Seagraf");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> zombieTokens() {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
    }
}
