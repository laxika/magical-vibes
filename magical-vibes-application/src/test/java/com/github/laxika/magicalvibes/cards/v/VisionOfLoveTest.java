package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionOfLove.class, Ornithopter.class, GrizzlyBears.class, Shock.class})
class VisionOfLoveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact draws two cards")
    void sacrificingArtifactDrawsTwoCards() {
        Ornithopter artifactCard = new Ornithopter();
        Shock firstDraw = new Shock();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new VisionOfLove())));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, artifactCard);

        castVisionOfLove();
        acceptMay();
        harness.handleListChoice(player1, "Sacrifice an artifact. If you do, draw two cards");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Discarding a card draws two cards")
    void discardingCardDrawsTwoCards() {
        VisionOfLove vision = new VisionOfLove();
        Ornithopter discarded = new Ornithopter();
        Shock firstDraw = new Shock();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(vision, discarded)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        castVisionOfLove();
        acceptMay();
        harness.handleListChoice(player1, "Discard a card. If you do, draw two cards");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Declining does not sacrifice, discard, or draw")
    void decliningDoesNothing() {
        VisionOfLove vision = new VisionOfLove();
        Ornithopter artifact = new Ornithopter();
        GrizzlyBears inHand = new GrizzlyBears();
        Shock onTop = new Shock();
        harness.setHand(player1, new ArrayList<>(List.of(vision, inHand)));
        harness.setLibrary(player1, List.of(onTop));
        harness.addToBattlefield(player1, artifact);

        castVisionOfLove();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
    }

    private void castVisionOfLove() {
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }

    private void acceptMay() {
        harness.handleMayAbilityChosen(player1, true);
    }
}
