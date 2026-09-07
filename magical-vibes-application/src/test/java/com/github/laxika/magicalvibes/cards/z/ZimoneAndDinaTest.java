package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneAndDina.class, Forest.class, GrizzlyBears.class})
class ZimoneAndDinaTest extends BaseCardTest {

    @Test
    @DisplayName("Drains an opponent when its controller draws their second card")
    void drainsOnSecondCardDraw() {
        harness.addToBattlefield(player1, new ZimoneAndDina());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws and optionally puts a tapped land after sacrificing another creature")
    void drawsAndPutsLandTapped() {
        Permanent source = addReadyZimoneAndDina(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        addForests(player1, 6);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(source.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Forest")).hasSize(7);
        assertThat(findPermanents(player1, "Forest").getLast().isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Repeats the draw and land choice once after reaching eight lands")
    void repeatsOnceAtEightLands() {
        Permanent source = addReadyZimoneAndDina(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        addForests(player1, 7);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        resolveLandChoice(true, 0);
        resolveLandChoice(true, 0);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Forest")).hasSize(9);
        assertThat(findPermanents(player1, "Forest")).filteredOn(Permanent::isTapped).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyZimoneAndDina(Player player) {
        return addCreatureReady(player, new ZimoneAndDina());
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private void resolveLandChoice(boolean accept, int cardIndex) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        if (accept) {
            harness.passBothPriorities();
            harness.handleCardChosen(player1, cardIndex);
        }
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
