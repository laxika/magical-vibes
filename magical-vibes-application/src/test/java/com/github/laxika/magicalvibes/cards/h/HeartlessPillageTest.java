package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartlessPillage.class, ColossalDreadmaw.class, JungleDelver.class})
class HeartlessPillageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting opponent")
    void castingPutsOnStack() {
        castHeartlessPillage();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new HeartlessPillage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Opponent discards two cards without raid — no treasure created")
    void discardWithoutRaid() {
        harness.setHand(player2, List.of(new ColossalDreadmaw(), new JungleDelver()));
        castHeartlessPillage();
        harness.passBothPriorities();

        // Opponent prompted to discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Jungle Delver");

        // No treasure tokens without raid
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("No discard prompt when opponent has empty hand — no treasure without raid")
    void emptyHandNoRaid() {
        harness.setHand(player2, List.of());
        castHeartlessPillage();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();

        // No treasure tokens without raid
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Opponent discards two cards and a Treasure token is created with raid")
    void discardWithRaid() {
        harness.setHand(player2, List.of(new ColossalDreadmaw(), new JungleDelver()));
        markAttackedThisTurn();
        castHeartlessPillage();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // One Treasure token created for the caster
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        Permanent treasure = treasures.getFirst();
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure token created even when opponent has empty hand with raid")
    void emptyHandWithRaid() {
        harness.setHand(player2, List.of());
        markAttackedThisTurn();
        castHeartlessPillage();
        harness.passBothPriorities();

        // No discard prompt
        assertThat(gameLogContains("no cards to discard")).isTrue();

        // Treasure token still created because raid is met
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
    }

    @Test
    @DisplayName("Raid remains satisfied after the attacking creature dies")
    void raidRemainsAfterAttackerDies() {
        harness.setHand(player2, List.of(new ColossalDreadmaw(), new JungleDelver()));
        addCreatureReady(player1, new JungleDelver());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertInGraveyard(player1, "Jungle Delver");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        castHeartlessPillage();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Declaring no attackers does not enable raid")
    void declaringNoAttackersDoesNotEnableRaid() {
        harness.setHand(player2, List.of(new ColossalDreadmaw(), new JungleDelver()));
        addCreatureReady(player1, new JungleDelver());
        declareAttackers(List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        castHeartlessPillage();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Opponent attacking does not enable raid for caster")
    void opponentAttackingDoesNotEnableRaid() {
        harness.setHand(player2, List.of(new ColossalDreadmaw(), new JungleDelver()));
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castHeartlessPillage();
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        // No treasure — opponent attacked, not caster
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new ColossalDreadmaw(), new JungleDelver()));
        castHeartlessPillage();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Heartless Pillage");
    }

    @Test
    @DisplayName("A one-card hand discards its only card and raid still creates Treasure")
    void oneCardHandWithRaid() {
        harness.setHand(player2, List.of(new JungleDelver()));
        markAttackedThisTurn();
        castHeartlessPillage();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Jungle Delver");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Heartless Pillage");
    }

    @Test
    @DisplayName("The opponent chooses exactly two cards to discard")
    void opponentChoosesTwoCards() {
        JungleDelver kept = new JungleDelver();
        harness.setHand(player2, List.of(kept, new ColossalDreadmaw(), new HeartlessPillage()));
        castHeartlessPillage();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Heartless Pillage");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The created Treasure sacrifices to add mana without using the stack")
    void treasureCanProduceMana() {
        harness.setHand(player2, List.of());
        markAttackedThisTurn();
        castHeartlessPillage();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castHeartlessPillage() {
        harness.setHand(player1, List.of(new HeartlessPillage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
