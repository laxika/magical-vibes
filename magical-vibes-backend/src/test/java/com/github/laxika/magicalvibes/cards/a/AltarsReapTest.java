package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AltarsReapTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Altar's Reap sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Altar's Reap");

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Resolving Altar's Reap draws two cards")
    void resolvingDrawsTwoCards() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        harness.assertInGraveyard(player1, "Altar's Reap");
    }

    @Test
    @DisplayName("Cannot cast Altar's Reap without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        // Add a creature to opponent's battlefield so spell is considered playable by ValidTargetService,
        // but player1 still has no creature to sacrifice
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature for Altar's Reap")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
