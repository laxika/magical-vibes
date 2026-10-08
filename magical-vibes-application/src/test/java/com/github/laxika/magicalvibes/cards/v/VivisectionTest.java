package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CopperCarapace;
import com.github.laxika.magicalvibes.cards.p.PlagueMyr;
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

@CardUsed({Vivisection.class, PlagueMyr.class, CopperCarapace.class})
class VivisectionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Vivisection sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PlagueMyr());

        harness.setHand(player1, List.of(new Vivisection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vivisection");

        harness.assertNotOnBattlefield(player1, "Plague Myr");
        harness.assertInGraveyard(player1, "Plague Myr");
    }

    @Test
    @DisplayName("Resolving Vivisection draws three cards")
    void resolvingDrawsThreeCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PlagueMyr());

        harness.setHand(player1, List.of(new Vivisection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Hand should have 3 cards (Vivisection was cast from hand, then 3 drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Deck should have 3 fewer cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        // Vivisection should be in graveyard
        harness.assertInGraveyard(player1, "Vivisection");
    }

    @Test
    @DisplayName("Cannot cast Vivisection without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        // Add a creature to opponent's battlefield so spell is considered playable by ValidTargetService,
        // but player1 still has no creature to sacrifice
        harness.addToBattlefield(player2, new PlagueMyr());
        harness.setHand(player1, List.of(new Vivisection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature for Vivisection")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PlagueMyr());

        harness.setHand(player1, List.of(new Vivisection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature artifact for Vivisection")
    void cannotSacrificeNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CopperCarapace());
        harness.addToBattlefield(player1, new PlagueMyr());
        harness.setHand(player1, List.of(new Vivisection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Copper Carapace");
        harness.assertOnBattlefield(player1, "Plague Myr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped creature can be sacrificed and cards are drawn only on resolution")
    void tappedCreaturePaysCostBeforeCardsAreDrawn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PlagueMyr());
        sacrifice.tap();
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new PlagueMyr());
        harness.setHand(player1, List.of(new Vivisection()));
        harness.setLibrary(player1, List.of(new CopperCarapace(), new PlagueMyr(), new Vivisection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player1, "Plague Myr");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player1, "Vivisection");
    }
}
