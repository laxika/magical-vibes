package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.s.SilverchaseFox;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltarsReap.class, SilverchaseFox.class, Dissipate.class, Swamp.class})
class AltarsReapTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Altar's Reap sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Altar's Reap");

        harness.assertNotOnBattlefield(player1, "Silverchase Fox");
        harness.assertInGraveyard(player1, "Silverchase Fox");
    }

    @Test
    @DisplayName("Resolving Altar's Reap draws two cards")
    void resolvingDrawsTwoCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());

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
        harness.addToBattlefield(player2, new SilverchaseFox());
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
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SilverchaseFox());

        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("A tapped creature can pay the sacrifice cost")
    void canSacrificeTappedCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        sacrifice.tap();
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Altar's Reap");
    }

    @Test
    @DisplayName("A noncreature cannot pay the sacrifice cost")
    void cannotSacrificeNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new SilverchaseFox());
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Silverchase Fox");
        harness.assertInHand(player1, "Altar's Reap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Countering the spell does not refund its sacrificed creature or draw cards")
    void counteringDoesNotRefundSacrificeOrDraw() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        AltarsReap reap = new AltarsReap();
        harness.setHand(player1, List.of(reap));
        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, reap.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.assertNotOnBattlefield(player1, "Silverchase Fox");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(reap);
        assertThat(gd.stack).isEmpty();
    }
}
