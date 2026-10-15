package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongbirdsBlessing.class, GrizzlyBears.class, HolyStrength.class, Plains.class})
class SongbirdsBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reveals until an Aura and declining puts it into hand")
    void declinesBattlefieldAndPutsAuraIntoHand() {
        addBlessingAndAttacker();
        Card plains = new Plains();
        Card aura = new HolyStrength();
        harness.setLibrary(player1, List.of(plains, aura));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Holy Strength");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("Accepting puts the revealed Aura onto the battlefield")
    void acceptsBattlefieldPlacement() {
        Permanent attacker = addBlessingAndAttacker();
        addCreatureReady(player2, new GrizzlyBears());
        Card plains = new Plains();
        Card aura = new HolyStrength();
        harness.setLibrary(player1, List.of(plains, aura));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.assertOnBattlefield(player1, "Holy Strength");
        harness.assertNotInGraveyard(player1, "Holy Strength");
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(attacker.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("Without an Aura, all revealed cards return to the bottom")
    void bottomsAllCardsWhenNoAuraIsFound() {
        addBlessingAndAttacker();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(plains, bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Grizzly Bears");
    }

    @Test
    @DisplayName("Revealing stops at the first Aura and leaves unrevealed cards above the bottomed cards")
    void stopsAtFirstAuraAndBottomsOnlyRevealedCards() {
        addBlessingAndAttacker();
        Card firstAura = new HolyStrength();
        Card secondAura = new HolyStrength();
        Card unrevealed = new GrizzlyBears();
        Card firstRevealed = new Plains();
        Card secondRevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstRevealed, secondRevealed, firstAura, secondAura, unrevealed));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstAura).doesNotContain(secondAura);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(secondAura, unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(firstRevealed, secondRevealed);
    }

    @Test
    @DisplayName("An empty library produces no optional placement choice")
    void emptyLibraryProducesNoChoice() {
        addBlessingAndAttacker();
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("An unenchanted creature attacking does not trigger the Blessing")
    void unrelatedAttackerDoesNotTrigger() {
        addBlessingAndAttacker();
        addCreatureReady(player1, new GrizzlyBears());
        Card aura = new HolyStrength();
        harness.setLibrary(player1, List.of(aura));

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The Blessing's controller reveals their library when an opponent's enchanted creature attacks")
    void opponentAttackerUsesAuraControllersLibrary() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new SongbirdsBlessing());
        blessing.setAttachedTo(attacker.getId());
        Card aura = new HolyStrength();
        Card opponentCard = new Plains();
        harness.setLibrary(player1, List.of(aura));
        harness.setLibrary(player2, List.of(opponentCard));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
    }

    private Permanent addBlessingAndAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new SongbirdsBlessing());
        blessing.setAttachedTo(attacker.getId());
        return attacker;
    }
}
