package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackbloomRogue.class, BlackbloomBog.class})
class BlackbloomRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 when an opponent has eight cards in their graveyard")
    void thresholdBoost() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new BlackbloomRogue());
        harness.setGraveyard(player2, graveyardOfSize(7));

        assertStats(rogue, 2, 3);

        harness.setGraveyard(player2, graveyardOfSize(8));

        assertStats(rogue, 5, 3);
    }

    @Test
    @DisplayName("The controller's graveyard does not enable the threshold boost")
    void ownGraveyardDoesNotCount() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new BlackbloomRogue());
        harness.setGraveyard(player1, graveyardOfSize(8));

        assertStats(rogue, 2, 3);
    }

    @Test
    @DisplayName("The boost disappears immediately when the opponent falls below eight cards")
    void thresholdBoostDisappears() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new BlackbloomRogue());
        harness.setGraveyard(player2, graveyardOfSize(9));

        assertStats(rogue, 5, 3);

        harness.setGraveyard(player2, graveyardOfSize(7));

        assertStats(rogue, 2, 3);
    }

    @Test
    @DisplayName("The graveyard condition uses the creature's controller")
    void opponentRelativeToController() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player2, new BlackbloomRogue());
        harness.setGraveyard(player2, graveyardOfSize(8));
        assertStats(rogue, 2, 3);

        harness.setGraveyard(player1, graveyardOfSize(8));
        assertStats(rogue, 5, 3);
    }

    @Test
    @DisplayName("The front face casts as a creature for three mana and receives its static boost")
    void creatureFaceResolves() {
        harness.setHand(player1, List.of(new BlackbloomRogue()));
        harness.setGraveyard(player2, graveyardOfSize(8));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent rogue = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(rogue.isTapped()).isFalse();
        assertStats(rogue, 5, 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new BlackbloomRogue());
        addCreatureReady(player2, new BlackbloomRogue());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new BlackbloomRogue());
        Permanent firstBlocker = addCreatureReady(player2, new BlackbloomRogue());
        Permanent secondBlocker = addCreatureReady(player2, new BlackbloomRogue());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Blackbloom Bog enters tapped and produces black mana")
    void landFaceEntersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new BlackbloomRogue()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(BlackbloomBog.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Playing the land face uses the land drop, bypasses the stack, and needs no mana")
    void landFaceUsesLandDrop() {
        harness.setHand(player1, List.of(new BlackbloomRogue(), new BlackbloomRogue()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new BlackbloomRogue());
        }
        return cards;
    }

    private void assertStats(Permanent rogue, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, rogue)).isEqualTo(toughness);
    }
}
