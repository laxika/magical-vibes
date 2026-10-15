package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Aethersnatch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TranscendentDragon.class, GrizzlyBears.class, Aethersnatch.class})
class TranscendentDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Cast ETB counters, exiles, and grants a free cast of the target spell")
    void castEtbCountersExilesAndGrantsFreeCast() {
        GrizzlyBears bears = new GrizzlyBears();
        castDragonAgainstSpell(bears);
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(bears.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Rejects a permanent as the ETB target")
    void rejectsPermanentTarget() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TranscendentDragon()));
        addDragonMana(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the cast leaves the card exiled with no later casting permission")
    void decliningCastDoesNotAllowCastingLater() {
        GrizzlyBears bears = new GrizzlyBears();
        castDragonAgainstSpell(bears);
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering without being cast does not counter a spell")
    void enteringWithoutBeingCastDoesNotTrigger() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.enterBattlefieldAndReturn(player2, new TranscendentDragon());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast and resolve when there is no other spell to counter")
    void resolvesWithoutAnotherSpell() {
        harness.castFromHand(player1, new TranscendentDragon(), "{4}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Transcendent Dragon");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Dragon spell stolen before resolution does not trigger for a controller who did not cast it")
    void stolenDragonDoesNotTriggerForNewController() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        TranscendentDragon dragon = new TranscendentDragon();
        harness.castFromHand(player1, dragon, "{4}{U}{U}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Aethersnatch()));
        addDragonMana(player2);
        harness.castAndResolveInstant(player2, 0, dragon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Transcendent Dragon");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
    private void castDragonAgainstSpell(GrizzlyBears bears) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castFromHand(player2, new TranscendentDragon(), "{4}{U}{U}");
        harness.passBothPriorities();
    }

    private void addDragonMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.addMana(player, ManaColor.BLUE, 2);
    }
}
