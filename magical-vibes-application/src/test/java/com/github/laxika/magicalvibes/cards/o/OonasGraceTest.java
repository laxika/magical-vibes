package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.f.FloodedGrove;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OonasGrace.class, DuskdaleWurm.class, FloodedGrove.class})
class OonasGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws a card")
    void targetPlayerDrawsACard() {
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.setLibrary(player2, List.of(new DuskdaleWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Oona's Grace");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent wurm = addCreatureReady(player2, new DuskdaleWurm());
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Retrace draws a card and discards a land, returning Oona's Grace to the graveyard")
    void retraceDrawsAndDiscardsLand() {
        harness.setGraveyard(player1, List.of(new OonasGrace()));
        harness.setHand(player1, List.of(new FloodedGrove()));
        harness.setLibrary(player1, List.of(new DuskdaleWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castRetrace(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        // Discarded the nonbasic land, drew one card: net zero.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertNotInHand(player1, "Flooded Grove");
        harness.assertInGraveyard(player1, "Flooded Grove");
        // Retrace returns the spell to the graveyard, not exile.
        harness.assertInGraveyard(player1, "Oona's Grace");
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new OonasGrace()));
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting from hand can target its controller without discarding a land")
    void castFromHandTargetsControllerWithoutDiscard() {
        FloodedGrove land = new FloodedGrove();
        DuskdaleWurm drawnCard = new DuskdaleWurm();
        harness.setHand(player1, List.of(new OonasGrace(), land));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, drawnCard);
        harness.assertInGraveyard(player1, "Oona's Grace");
        harness.assertNotInGraveyard(player1, "Flooded Grove");
    }

    @Test
    @DisplayName("Oona's Grace can be retraced again after resolving")
    void canRetraceRepeatedly() {
        OonasGrace grace = new OonasGrace();
        FloodedGrove firstLand = new FloodedGrove();
        FloodedGrove secondLand = new FloodedGrove();
        DuskdaleWurm firstDraw = new DuskdaleWurm();
        DuskdaleWurm secondDraw = new DuskdaleWurm();
        harness.setGraveyard(player1, List.of(grace));
        harness.setHand(player1, List.of(firstLand, secondLand));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castRetrace(player1, 0, 0, player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLand);
        harness.assertInGraveyard(player1, "Flooded Grove");
        harness.assertNotInGraveyard(player1, "Oona's Grace");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        int graceIndex = gd.playerGraveyards.get(player1.getId()).indexOf(grace);
        harness.castRetrace(player1, graceIndex, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, secondLand, grace);
    }

    @Test
    @DisplayName("Retrace still requires the full mana cost and does not discard on a rejected cast")
    void retraceRequiresFullManaCost() {
        OonasGrace grace = new OonasGrace();
        FloodedGrove land = new FloodedGrove();
        harness.setGraveyard(player1, List.of(grace));
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(grace);
        assertThat(gd.stack).isEmpty();
    }
}
