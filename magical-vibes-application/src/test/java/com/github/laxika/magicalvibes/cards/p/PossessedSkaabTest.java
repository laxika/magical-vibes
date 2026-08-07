package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PossessedSkaabTest extends BaseCardTest {

    /** Casts Possessed Skaab and resolves it so its ETB trigger sets up graveyard targeting. */
    private void castSkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PossessedSkaab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted creature card from the graveyard to hand")
    void etbReturnsCreatureToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castSkaab();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB can return an instant card from the graveyard to hand")
    void etbReturnsInstantToHand() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));

        castSkaab();

        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("A card that is not an instant, sorcery, or creature is not a legal target")
    void nonMatchingCardNotTargetable() {
        harness.setGraveyard(player1, List.of(new com.github.laxika.magicalvibes.cards.i.IcyManipulator()));

        castSkaab();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("When Possessed Skaab would die, it is exiled instead")
    void exiledInsteadOfDying() {
        harness.addToBattlefield(player1, new PossessedSkaab());

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Possessed Skaab");
        harness.assertNotInGraveyard(player1, "Possessed Skaab");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Possessed Skaab"));
    }

    @Test
    @DisplayName("A countered Possessed Skaab goes to the graveyard — the replacement only covers dying")
    void counteredSkaabGoesToGraveyard() {
        PossessedSkaab skaab = new PossessedSkaab();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(skaab));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, skaab.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Possessed Skaab");
    }

    @Test
    @DisplayName("Exiling Possessed Skaab from the battlefield still exiles it, not replaced twice")
    void exiledByRemovalStaysExiled() {
        harness.addToBattlefield(player1, new PossessedSkaab());
        var skaab = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, skaab.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Possessed Skaab");
        harness.assertNotInGraveyard(player1, "Possessed Skaab");
    }
}
