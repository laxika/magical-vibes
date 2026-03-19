package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WreathOfGeistsTest extends BaseCardTest {

    @Test
    @DisplayName("Wreath of Geists has correct card properties")
    void hasCorrectProperties() {
        WreathOfGeists card = new WreathOfGeists();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostCreaturePerCardsInControllerGraveyardEffect.class);
    }

    @Test
    @DisplayName("Casting Wreath of Geists puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new WreathOfGeists()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wreath of Geists");
    }

    @Test
    @DisplayName("Enchanted creature gets +X/+X where X is creature cards in controller's graveyard")
    void boostsPerCreatureCardInGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Put 2 creature cards in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        Permanent wreath = new Permanent(new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wreath);

        // Grizzly Bears is 2/2 + 2 creature cards in graveyard = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-creature cards in graveyard do not count")
    void nonCreatureCardsDoNotCount() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Put 1 creature and 1 non-creature in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        Permanent wreath = new Permanent(new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wreath);

        // Only 1 creature card counts: 2/2 + 1 = 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost updates dynamically as graveyard changes")
    void updatesDynamicallyWithGraveyardChanges() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent wreath = new Permanent(new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wreath);

        // No creatures in graveyard: 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Add 1 creature to graveyard: 3/3
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        // Add another creature: 4/4
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        // Remove all creatures from graveyard: back to 2/2
        gd.playerGraveyards.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's graveyard creatures do not count")
    void opponentGraveyardDoesNotCount() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Opponent has 3 creature cards in graveyard
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        Permanent wreath = new Permanent(new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wreath);

        // Only controller's graveyard counts: 2/2 + 0 = 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect ends when aura leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        Permanent wreath = new Permanent(new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wreath);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(wreath);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wreath of Geists counts controller's graveyard even when enchanting opponent's creature")
    void countsControllersGraveyardOnOpponentCreature() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        // Controller (player1) has 2 creature cards in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        Permanent wreath = new Permanent(new WreathOfGeists());
        wreath.setAttachedTo(opponentBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wreath);

        // Opponent's bears get boosted by controller's graveyard: 2/2 + 2 = 4/4
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }
}
