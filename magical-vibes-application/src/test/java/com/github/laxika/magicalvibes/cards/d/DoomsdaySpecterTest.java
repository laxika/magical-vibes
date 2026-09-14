package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AmphibiousKavu;
import com.github.laxika.magicalvibes.cards.n.NightscapeFamiliar;
import com.github.laxika.magicalvibes.cards.s.Singe;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomsdaySpecter.class, StormscapeFamiliar.class, NightscapeFamiliar.class,
        AmphibiousKavu.class, TerminalMoraine.class, DaringLeap.class, Singe.class})
class DoomsdaySpecterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return a blue or black creature you control")
    void etbPromptsForBlueOrBlackCreature() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar()).getId();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar()).getId();
        harness.addToBattlefield(player1, new AmphibiousKavu());
        harness.addToBattlefield(player1, new TerminalMoraine());
        harness.addToBattlefield(player2, new StormscapeFamiliar());

        castAndResolveSpell();

        UUID specterId = harness.getPermanentId(player1, "Doomsday Specter");
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(blueId, blackId, specterId);
    }

    @Test
    @DisplayName("Choosing a matching creature returns it to its owner's hand")
    void chosenCreatureReturnsToHand() {
        UUID familiarId = harness.addToBattlefieldAndReturn(player1, new NightscapeFamiliar()).getId();

        castAndResolveSpell();
        harness.handlePermanentChosen(player1, familiarId);

        harness.assertInHand(player1, "Nightscape Familiar");
        harness.assertOnBattlefield(player1, "Doomsday Specter");
    }

    @Test
    @DisplayName("ETB can return Doomsday Specter itself")
    void etbCanReturnItself() {
        castAndResolveSpell();

        UUID specterId = harness.getPermanentId(player1, "Doomsday Specter");
        harness.handlePermanentChosen(player1, specterId);

        harness.assertInHand(player1, "Doomsday Specter");
        harness.assertNotOnBattlefield(player1, "Doomsday Specter");
    }

    @Test
    @DisplayName("Combat damage lets the controller choose a card for the damaged player to discard")
    void combatDamagePromptsControllerChoiceAndDiscardsChosenCard() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new DaringLeap(), new Singe()));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.discardMode()).isTrue();
        assertThat(choice.exileMode()).isFalse();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Daring Leap");
        harness.assertInHand(player2, "Singe");
    }

    @Test
    @DisplayName("Combat damage does not prompt when the damaged player's hand is empty")
    void combatDamageWithEmptyHandDoesNotPrompt() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new DoomsdaySpecter(), "{2}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new DoomsdaySpecter());
        specter.setAttacking(true);
        return specter;
    }
}
