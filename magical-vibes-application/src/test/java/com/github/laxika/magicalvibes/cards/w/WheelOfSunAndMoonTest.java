package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WheelOfSunAndMoon.class, CruelEdict.class, GrizzlyBears.class, Millstone.class,
        Demystify.class, MindRot.class, RuleOfLaw.class})
class WheelOfSunAndMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wheel of Sun and Moon attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new WheelOfSunAndMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wheel of Sun and Moon")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    @Test
    @DisplayName("Enchanted player's milled card is put on the bottom of their library, not the graveyard")
    void milledCardGoesToBottomOfLibrary() {
        placeWheelOnPlayer(player2, player2); // controlled by and enchanting player2
        addReadyMillstone(player1);            // player1's only permanent → index 0
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Nothing hit the graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // The milled card is now on the bottom of the library
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(2);
        assertThat(deck.get(deck.size() - 1).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Enchanted player's creature is put on the bottom of their library instead of dying")
    void dyingCreatureGoesToBottomOfLibrary() {
        placeWheelOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A non-enchanted player's milled cards still go to the graveyard")
    void nonEnchantedPlayerUnaffected() {
        placeWheelOnPlayer(player2, player2); // Wheel enchants player2, not player1
        addReadyMillstone(player1);           // player1's only permanent → index 0
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // Wheel enchants player2, so player1's milled card hits the graveyard normally
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A destroyed Wheel enchanting its owner replaces its own graveyard move")
    void destroyedWheelGoesToItsOwnersLibrary() {
        Permanent wheel = placeWheelOnPlayer(player2, player2);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, wheel.getId());

        harness.assertNotOnBattlefield(player2, "Wheel of Sun and Moon");
        harness.assertNotInGraveyard(player2, "Wheel of Sun and Moon");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(wheel.getCard());
    }

    @Test
    @DisplayName("The enchanted player chooses the bottom order of simultaneously milled cards")
    void enchantedPlayerChoosesMilledCardOrder() {
        placeWheelOnPlayer(player1, player2);
        addReadyMillstone(player1);
        GrizzlyBears first = new GrizzlyBears();
        RuleOfLaw second = new RuleOfLaw();
        CruelEdict remaining = new CruelEdict();
        harness.setLibrary(player2, List.of(first, second, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.toBottom()).isTrue();
        assertThat(choice.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining, second, first);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A resolving sorcery goes to the bottom of its enchanted owner's library")
    void resolvedSpellGoesToBottom() {
        placeWheelOnPlayer(player2, player1);
        CruelEdict spell = new CruelEdict();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarded cards go to the bottom of the enchanted player's library")
    void discardedCardsGoToBottom() {
        placeWheelOnPlayer(player1, player2);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Destroying a Wheel enchanting another player ends its replacement effect")
    void replacementStopsWhenWheelLeavesBattlefield() {
        Permanent wheel = placeWheelOnPlayer(player1, player2);
        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, wheel.getId());
        harness.assertInGraveyard(player1, "Wheel of Sun and Moon");

        addReadyMillstone(player1);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    private Permanent placeWheelOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent wheel = harness.addToBattlefieldAndReturn(controller, new WheelOfSunAndMoon());
        wheel.setAttachedTo(enchantedPlayer.getId());
        return wheel;
    }

    private Permanent addReadyMillstone(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Millstone());
        perm.setSummoningSick(false);
        return perm;
    }
}
