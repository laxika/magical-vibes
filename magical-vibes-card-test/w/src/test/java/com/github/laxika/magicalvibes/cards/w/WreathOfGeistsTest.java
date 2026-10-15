package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.t.TravelPreparations;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WreathOfGeists.class, DarkthicketWolf.class, TravelPreparations.class})
class WreathOfGeistsTest extends BaseCardTest {

    @Test
    @DisplayName("Aura resolves on an opponent's creature using the current controller graveyard")
    void resolvesUsingCurrentGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));
        harness.setGraveyard(player2, List.of(new DarkthicketWolf(), new DarkthicketWolf(), new DarkthicketWolf()));
        harness.setHand(player1, List.of(new WreathOfGeists()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of(new DarkthicketWolf(), new DarkthicketWolf(), new TravelPreparations()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WreathOfGeists)
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting Wreath of Geists puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new WreathOfGeists()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Enchanted creature gets +X/+X where X is creature cards in controller's graveyard")
    void boostsPerCreatureCardInGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());

        // Put 2 creature cards in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());

        Permanent wreath = harness.addToBattlefieldAndReturn(player1, new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());

        // Darkthicket Wolf is 2/2 + 2 creature cards in graveyard = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-creature cards in graveyard do not count")
    void nonCreatureCardsDoNotCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());

        // Put 1 creature and 1 non-creature in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());
        gd.playerGraveyards.get(player1.getId()).add(new TravelPreparations());

        Permanent wreath = harness.addToBattlefieldAndReturn(player1, new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());

        // Only 1 creature card counts: 2/2 + 1 = 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost updates dynamically as graveyard changes")
    void updatesDynamicallyWithGraveyardChanges() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());

        Permanent wreath = harness.addToBattlefieldAndReturn(player1, new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());

        // No creatures in graveyard: 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // Add 1 creature to graveyard: 3/3
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        // Add another creature: 4/4
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());

        // Opponent has 3 creature cards in graveyard
        gd.playerGraveyards.get(player2.getId()).add(new DarkthicketWolf());
        gd.playerGraveyards.get(player2.getId()).add(new DarkthicketWolf());
        gd.playerGraveyards.get(player2.getId()).add(new DarkthicketWolf());

        Permanent wreath = harness.addToBattlefieldAndReturn(player1, new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());

        // Only controller's graveyard counts: 2/2 + 0 = 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect ends when aura leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());

        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());

        Permanent wreath = harness.addToBattlefieldAndReturn(player1, new WreathOfGeists());
        wreath.setAttachedTo(bears.getId());

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
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());

        // Controller (player1) has 2 creature cards in graveyard
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());
        gd.playerGraveyards.get(player1.getId()).add(new DarkthicketWolf());

        Permanent wreath = harness.addToBattlefieldAndReturn(player1, new WreathOfGeists());
        wreath.setAttachedTo(opponentBears.getId());

        // Opponent's bears get boosted by controller's graveyard: 2/2 + 2 = 4/4
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }
}
