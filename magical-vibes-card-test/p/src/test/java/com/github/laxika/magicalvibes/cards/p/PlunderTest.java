package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Plunder.class, AshcoatBear.class, Forest.class, PrismaticLens.class})
class PlunderTest extends BaseCardTest {

    @Test
    @DisplayName("Plunder destroys a target artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new Plunder()));
        addPlunderMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prismatic Lens");
    }

    @Test
    @DisplayName("Plunder destroys a target land")
    void destroysLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Plunder()));
        addPlunderMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Plunder cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Plunder()));
        addPlunderMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    @Test
    @DisplayName("Suspend removes a time counter only during Plunder's owner's upkeep")
    void suspendRemovesCounterOnlyDuringOwnerUpkeep() {
        Plunder card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that destroys an artifact")
    void lastCounterOffersFreeCastAndDestroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        Plunder card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prismatic Lens");
        harness.assertInGraveyard(player1, "Plunder");
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Plunder in exile")
    void decliningSuspendCastLeavesCardInExile() {
        Plunder card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotInGraveyard(player1, "Plunder");
    }

    @Test
    @DisplayName("Suspend exiles Plunder with four time counters")
    void suspendExilesWithFourTimeCounters() {
        Plunder card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plunder can destroy its controller's own land")
    void destroysOwnLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Plunder()));
        addPlunderMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Plunder");
    }

    @Test
    @DisplayName("The upkeep counter removal waits for its trigger to resolve")
    void counterRemovalUsesStack() {
        Plunder card = suspendCard();

        advanceToUpkeep(player1);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("Plunder cannot normally be suspended during an opponent's turn")
    void cannotSuspendDuringOpponentsTurn() {
        Plunder card = new Plunder();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be suspended");

        harness.assertInHand(player1, "Plunder");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @CardUsed({PithingNeedle.class})
    @DisplayName("Pithing Needle cannot prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspend() {
        harness.castFromHand(player1, new PithingNeedle(), "{1}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Plunder");

        Plunder card = suspendCard();

        harness.assertNotInHand(player1, "Plunder");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    private Plunder suspendCard() {
        Plunder card = new Plunder();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void addPlunderMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
