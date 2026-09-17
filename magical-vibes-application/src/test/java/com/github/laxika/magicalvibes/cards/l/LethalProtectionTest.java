package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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

@CardUsed({LethalProtection.class, GrizzlyBears.class, HolyDay.class})
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
        gd.playerGraveyards.get(player1.getId()).clear();
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
        Card noncreature = new HolyDay();
        harness.addToBattlefield(player2, noncreature);
        harness.setHand(player1, List.of(new LethalProtection()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID noncreaturePermanentId = harness.getPermanentId(player2, "Holy Day");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(noncreaturePermanentId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
