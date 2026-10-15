package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LethalProtection.class, GrizzlyBears.class, HolyDay.class, Plains.class})
class LethalProtectionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and returns a target creature card from the graveyard to hand")
    void destroysAndReturnsCreature() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID battlefieldCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(battlefieldCreatureId));
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("May omit the optional graveyard target")
    void mayOmitGraveyardTarget() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID battlefieldCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(battlefieldCreatureId));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("A missing graveyard target does not stop the destruction")
    void missingGraveyardTargetDoesNotStopDestruction() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID battlefieldCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(battlefieldCreatureId));
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Offers only creature cards as graveyard targets")
    void offersOnlyCreatureGraveyardTargets() {
        Card creature = new GrizzlyBears();
        Card noncreature = new HolyDay();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID battlefieldCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(battlefieldCreatureId));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Rejects a noncreature permanent target")
    void rejectsNoncreaturePermanentTarget() {
        Card noncreature = new Plains();
        harness.addToBattlefield(player2, noncreature);
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID noncreaturePermanentId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(noncreaturePermanentId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a creature with an empty graveyard")
    void destroysWithEmptyGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns the graveyard target even if the battlefield target leaves")
    void returnsWhenBattlefieldTargetLeaves() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not resolve when its only chosen target leaves")
    void doesNotResolveWhenOnlyTargetLeaves() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));
        harness.handleMultipleCardsChosen(player1, List.of());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lethal Protection");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose a creature card in the opponent's graveyard")
    void excludesOpponentGraveyard() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Cannot return the creature it just destroyed")
    void doesNotReturnNewlyDestroyedCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, creature);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(harness.getPermanentId(player1, "Grizzly Bears")));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot choose more than one graveyard creature")
    void rejectsTwoGraveyardTargets() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(first.getId()))
                .noneMatch(card -> card.getId().equals(second.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Requires a battlefield creature target even with a creature in the graveyard")
    void requiresBattlefieldCreatureTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lethal Protection");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
