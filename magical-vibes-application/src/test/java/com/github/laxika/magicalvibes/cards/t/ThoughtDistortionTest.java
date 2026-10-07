package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.m.ManifoldKey;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtDistortion.class, Cancel.class, GrizzlyBears.class, Island.class, Peek.class,
        Shock.class, LeylineOfSanctity.class, ManifoldKey.class, Pacifism.class})
class ThoughtDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles noncreature, nonland cards from the target's hand and graveyard")
    void exilesMatchingCardsFromHandAndGraveyard() {
        Card handSpell = new Shock();
        Card handCreature = new GrizzlyBears();
        Card handLand = new Island();
        Card graveyardSpell = new Peek();
        Card graveyardCreature = new GrizzlyBears();
        Card graveyardLand = new Island();

        harness.setHand(player2, List.of(handSpell, handCreature, handLand));
        harness.setGraveyard(player2, List.of(graveyardSpell, graveyardCreature, graveyardLand));
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCreature, handLand);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(graveyardCreature, graveyardLand);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(handSpell, graveyardSpell);
    }

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCountered() {
        ThoughtDistortion distortion = new ThoughtDistortion();
        harness.setHand(player1, List.of(distortion));
        harness.setHand(player2, List.of(new Cancel(), new GrizzlyBears()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, distortion.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thought Distortion");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Cancel");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles artifacts and enchantments without affecting the controller's zones")
    void exilesNoncreaturePermanentsOnlyFromOpponent() {
        Card handArtifact = new ManifoldKey();
        Card handEnchantment = new Pacifism();
        Card graveyardArtifact = new ManifoldKey();
        Card graveyardEnchantment = new Pacifism();
        Card ownHandSpell = new Shock();
        Card ownGraveyardSpell = new Shock();
        harness.setHand(player1, List.of(new ThoughtDistortion(), ownHandSpell));
        harness.setGraveyard(player1, List.of(ownGraveyardSpell));
        harness.setHand(player2, List.of(handArtifact, handEnchantment));
        harness.setGraveyard(player2, List.of(graveyardArtifact, graveyardEnchantment));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(
                handArtifact, handEnchantment, graveyardArtifact, graveyardEnchantment);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownHandSpell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyardSpell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty hand does not prevent exiling cards from the graveyard")
    void exilesGraveyardWithEmptyHand() {
        Card spell = new Shock();
        Card land = new Island();
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(spell, land));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        harness.assertInGraveyard(player1, "Thought Distortion");
    }

    @Test
    @DisplayName("Reveals the hand even when every card is a creature or land")
    void revealsHandWithoutMatchingCards() {
        Card creature = new GrizzlyBears();
        Card land = new Island();
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        harness.setHand(player2, List.of(creature, land));
        harness.setGraveyard(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature, land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals their hand")
                && entry.plainText().contains("Grizzly Bears")
                && entry.plainText().contains("Island"));
        harness.assertInGraveyard(player1, "Thought Distortion");
    }

    @Test
    @DisplayName("Does not resolve when its opponent gains hexproof before resolution")
    void doesNotResolveWithIllegalTargetDespiteBeingUncounterable() {
        Card handSpell = new Shock();
        Card graveyardSpell = new Shock();
        harness.setHand(player1, List.of(new ThoughtDistortion()));
        harness.setHand(player2, List.of(handSpell));
        harness.setGraveyard(player2, List.of(graveyardSpell));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardSpell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("reveals their hand"));
        harness.assertInGraveyard(player1, "Thought Distortion");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
