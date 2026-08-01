package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChronicFloodingTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Chronic Flooding targeting a land")
    void canTargetLand() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ChronicFlooding()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Cannot cast Chronic Flooding targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new ChronicFlooding()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Tapping the enchanted land mills its controller three cards")
    void tappingLandMillsController() {
        addLandWithAura(player1);
        harness.setLibrary(player1, library(6));

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The enchanted land's controller mills, not the Aura's controller")
    void millsLandControllerNotAuraController() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();

        Permanent aura = new Permanent(new ChronicFlooding());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setLibrary(player1, library(6));
        harness.setLibrary(player2, library(6));

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not mill")
    void tappingUnenchantedLandDoesNotMill() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setLibrary(player1, library(6));

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Chronic Flooding"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Chronic Flooding"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    /** Places a land on {@code owner}'s battlefield (index 0) with a Chronic Flooding attached. */
    private void addLandWithAura(Player owner) {
        harness.addToBattlefield(owner, new Mountain());
        Permanent land = gd.playerBattlefields.get(owner.getId()).getFirst();

        Permanent aura = new Permanent(new ChronicFlooding());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Mountain());
        }
        return cards;
    }

    /** Drives priority until the stack and any deferred mana-ability triggers are fully resolved. */
    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
