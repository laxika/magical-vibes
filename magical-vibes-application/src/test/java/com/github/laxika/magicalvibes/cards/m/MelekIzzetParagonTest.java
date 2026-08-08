package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MelekIzzetParagonTest extends BaseCardTest {

    private long boltsOnStack() {
        return gd.stack.stream().filter(e -> e.getCard().getName().equals("Lightning Bolt")).count();
    }

    @Test
    @DisplayName("Casting an instant from the top of the library copies it")
    void libraryTopInstantIsCopied() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        gd.playerDecks.get(player1.getId()).addFirst(new LightningBolt());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromLibraryTop(player1, player2.getId());
        harness.passBothPriorities(); // resolve the copy trigger

        assertThat(boltsOnStack()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copy resolves, so the spell's damage is dealt twice")
    void copyDealsDamageAgain() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        gd.playerDecks.get(player1.getId()).addFirst(new LightningBolt());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromLibraryTop(player1, player2.getId());
        harness.passBothPriorities(); // resolve the copy trigger
        harness.handleMayAbilityChosen(player1, false); // keep the copy's original target
        harness.passBothPriorities(); // resolve the copy
        harness.passBothPriorities(); // resolve the original

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Casting an instant from hand does not copy it")
    void handCastIsNotCopied() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(boltsOnStack()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature card on top of the library cannot be cast")
    void creatureOnTopIsNotCastable() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }
}
