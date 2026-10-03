package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PatchworkBeastie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Anthropede.class, DazzlingTheaterPropRoom.class, Forest.class, PatchworkBeastie.class})
class AnthropedeTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card creates a reflexive ability that destroys a target Room")
    void discardingCardDestroysTargetRoom() {
        Permanent room = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        harness.addToBattlefield(player2, new PatchworkBeastie());
        harness.setHand(player1, List.of(new Anthropede(), new Forest()));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(room.getId());

        harness.handlePermanentChosen(player1, room.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dazzling Theater");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Paying {2} creates a reflexive ability that destroys a target Room")
    void payingManaDestroysTargetRoom() {
        Permanent room = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        harness.setHand(player1, List.of(new Anthropede()));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Pay {2}");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, room.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dazzling Theater");
    }

    @Test
    @DisplayName("Declining the optional ability leaves the Room and hand unchanged")
    void decliningAbilityDoesNothing() {
        Permanent room = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        Forest cardInHand = new Forest();
        harness.setHand(player1, List.of(new Anthropede(), cardInHand));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(room);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
    }

    @Test
    @DisplayName("Discarding without a Room still discards the card without choosing a target")
    void canDiscardWithoutRooms() {
        harness.setHand(player1, List.of(new Anthropede(), new Forest()));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying without a Room still spends two mana without choosing a target")
    void canPayWithoutRooms() {
        harness.setHand(player1, List.of(new Anthropede()));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Pay {2}");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the mana payment creates no destruction ability")
    void decliningManaPaymentDoesNothing() {
        Permanent room = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        harness.setHand(player1, List.of(new Anthropede(), new Forest()));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Pay {2}");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(room);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The reflexive trigger can destroy your own Room and leaves other Rooms alone")
    void canTargetOwnRoomWithSeparateReflexiveTrigger() {
        Permanent ownRoom = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        Permanent otherRoom = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        harness.setHand(player1, List.of(new Anthropede(), new Forest()));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Discard a card");
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ownRoom.getId(), otherRoom.getId());
        harness.handlePermanentChosen(player1, ownRoom.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownRoom);
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownRoom);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherRoom);
    }

    @Test
    @DisplayName("Paying mana does not discard a card and destruction waits for the reflexive trigger")
    void manaPaymentDoesNotDiscardAndUsesSeparateTrigger() {
        Permanent room = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        Forest cardInHand = new Forest();
        harness.setHand(player1, List.of(new Anthropede(), cardInHand));
        addAnthropedeMana();

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Pay {2}");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, room.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(room);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(room);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
    }

    private void addAnthropedeMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void castAndResolveToChoice() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
