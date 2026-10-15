package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HaloHopper;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZimoneAndDina.class, Forest.class, HaloHopper.class})
class ZimoneAndDinaTest extends BaseCardTest {

    @Test
    @DisplayName("Drains an opponent when its controller draws their second card")
    void drainsOnSecondCardDraw() {
        harness.addToBattlefield(player1, new ZimoneAndDina());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new HaloHopper(), new HaloHopper(), new HaloHopper()));

        draw(player1);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Draws and optionally puts a tapped land after sacrificing another creature")
    void drawsAndPutsLandTapped() {
        Permanent source = addReadyZimoneAndDina(player1);
        harness.addToBattlefield(player1, new HaloHopper());
        addForests(player1, 6);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new HaloHopper()));

        harness.activateAbility(player1, 0, null, null);
        resolveLandChoice(true, 0);

        assertThat(source.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Forest")).hasSize(7);
        assertThat(findPermanents(player1, "Forest").getLast().isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Halo Hopper");
        harness.assertInHand(player1, "Halo Hopper");
    }

    @Test
    @DisplayName("Repeats the draw and land choice once after reaching eight lands")
    void repeatsOnceAtEightLands() {
        Permanent source = addReadyZimoneAndDina(player1);
        harness.addToBattlefield(player1, new HaloHopper());
        addForests(player1, 7);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new HaloHopper(), new HaloHopper()));

        harness.activateAbility(player1, 0, null, null);
        resolveLandChoice(true, 0);
        resolveLandChoice(true, 0);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Forest")).hasSize(9);
        assertThat(findPermanents(player1, "Forest")).filteredOn(Permanent::isTapped).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Halo Hopper", "Halo Hopper");
        harness.assertInGraveyard(player1, "Halo Hopper");
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

    @Test
    @DisplayName("Declining the land at seven lands does not repeat the process")
    void decliningAtSevenLandsDoesNotRepeat() {
        addReadyZimoneAndDina(player1);
        harness.addToBattlefield(player1, new HaloHopper());
        addForests(player1, 7);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new HaloHopper(), new HaloHopper()));

        harness.activateAbility(player1, 0, null, null);
        resolveLandChoice(false, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(7);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Halo Hopper");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("At eight lands the process repeats even when both land choices are declined")
    void repeatsAtEightLandsWhenLandChoicesDeclined() {
        addReadyZimoneAndDina(player1);
        harness.addToBattlefield(player1, new HaloHopper());
        addForests(player1, 8);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new HaloHopper(), new HaloHopper(), new HaloHopper()));

        harness.activateAbility(player1, 0, null, null);
        resolveLandChoice(false, 0);
        resolveLandChoice(false, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(8);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Halo Hopper", "Halo Hopper");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Zimone and Dina")).hasSize(1);
        harness.assertInGraveyard(player1, "Halo Hopper");
    }

    @Test
    @DisplayName("Cannot activate with only Zimone and Dina available to sacrifice")
    void cannotSacrificeItself() {
        Permanent source = addReadyZimoneAndDina(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Zimone and Dina")).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger Zimone and Dina")
    void opponentsSecondDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZimoneAndDina());
        harness.setLibrary(player2, List.of(new HaloHopper(), new HaloHopper()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers when the first card was drawn before Zimone and Dina entered")
    void firstDrawBeforeEnteringStillCounts() {
        harness.setLibrary(player1, List.of(new HaloHopper(), new HaloHopper()));
        draw(player1);
        harness.addToBattlefield(player1, new ZimoneAndDina());

        draw(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
