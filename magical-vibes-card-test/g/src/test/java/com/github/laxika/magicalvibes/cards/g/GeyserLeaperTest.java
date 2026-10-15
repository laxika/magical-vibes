package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeyserLeaper.class, Plains.class, GliderStaff.class})
class GeyserLeaperTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps four artifacts or creatures and loots")
    void waterbendTapsFourPermanentsAndLoots() {
        Permanent leaper = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new GeyserLeaper()));
        harness.setLibrary(player1, List.of(new GeyserLeaper()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(leaper.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Waterbend cannot be paid without four available payments")
    void waterbendRequiresFourPayments() {
        Permanent leaper = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(leaper.isTapped()).isFalse();
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana while the source is tapped")
    void paysWithManaWhileTappedAndDiscardsDrawnCard() {
        Permanent leaper = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        leaper.tap();
        Plains keptCard = new Plains();
        GeyserLeaper drawnCard = new GeyserLeaper();
        harness.setHand(player1, List.of(keptCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Waterbend combines mana, a summoning sick creature, and an artifact")
    void paysWithManaCreatureAndArtifact() {
        Permanent leaper = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        leaper.setSummoningSick(true);
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new GliderStaff());
        harness.setHand(player1, List.of(new Plains()));
        harness.setLibrary(player1, List.of(new GeyserLeaper()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(leaper.isTapped()).isTrue();
        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Geyser Leaper");
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty hand still draws and then discards the drawn card")
    void emptyHandDrawsThenDiscards() {
        harness.addToBattlefield(player1, new GeyserLeaper());
        GeyserLeaper drawnCard = new GeyserLeaper();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    @Test
    @DisplayName("Tapped permanents and opposing creatures cannot pay waterbend")
    void excludesTappedAndOpposingPermanents() {
        Permanent leaper = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        harness.addToBattlefield(player1, new GeyserLeaper());
        harness.addToBattlefield(player1, new GeyserLeaper());
        Permanent tappedStaff = harness.addToBattlefieldAndReturn(player1, new GliderStaff());
        tappedStaff.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GeyserLeaper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(leaper.isTapped()).isFalse();
        assertThat(tappedStaff.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Waterbend can be activated repeatedly without tapping the source")
    void canActivateTwiceInOneTurn() {
        Permanent leaper = harness.addToBattlefieldAndReturn(player1, new GeyserLeaper());
        Plains keptCard = new Plains();
        GeyserLeaper firstDraw = new GeyserLeaper();
        GeyserLeaper secondDraw = new GeyserLeaper();
        harness.setHand(player1, List.of(keptCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(leaper.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

}
