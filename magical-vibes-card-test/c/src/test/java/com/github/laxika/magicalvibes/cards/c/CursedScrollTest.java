package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.f.FireIce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedScroll.class, Counterspell.class, FightingDrake.class, FireIce.class})
class CursedScrollTest extends BaseCardTest {

    @Test
    @DisplayName("A split card has each of its two names")
    void splitCardHasEitherChosenName() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new FireIce()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Ice");

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The names offered include a face-up spell's and leave out a face-down spell's")
    void offeredNamesLeaveOutFaceDownSpell() {
        StackEntry hidden = new StackEntry(StackEntryType.CREATURE_SPELL, unlistedCard("Hidden Spell"),
                player2.getId(), "Face-down spell", List.of());
        hidden.setCastFaceDown(true);
        gd.stack.add(hidden);
        gd.stack.add(new StackEntry(StackEntryType.CREATURE_SPELL, unlistedCard("Shown Spell"),
                player2.getId(), "Shown spell", List.of()));

        assertThat(namesOfferedByScroll()).contains("Shown Spell").doesNotContain("Hidden Spell");
    }

    @Test
    @DisplayName("The names offered leave out the source of a face-down permanent's ability")
    void offeredNamesLeaveOutFaceDownSourceOfAbility() {
        Card card = unlistedCard("Hidden Source");
        Permanent source = harness.addToBattlefieldAndReturn(player2, card);
        source.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        gd.stack.add(TestStackDoor.triggered(card, player2.getId(), "Ability", List.of(), 0, source.getId()));

        assertThat(namesOfferedByScroll()).doesNotContain("Hidden Source");
    }

    @Test
    @DisplayName("The names offered leave out a face-down permanent's card that an ability names as its source")
    void offeredNamesLeaveOutFaceDownCardAsSource() {
        Card card = unlistedCard("Hidden Source Card");
        Permanent source = harness.addToBattlefieldAndReturn(player2, card);
        source.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        gd.stack.add(TestStackDoor.triggered(card, player2.getId(), "Ability", List.of()));

        assertThat(namesOfferedByScroll()).doesNotContain("Hidden Source Card");
    }

    @Test
    @DisplayName("The names offered leave out the face-down source of an ability after it left the battlefield")
    void offeredNamesLeaveOutDepartedFaceDownSourceOfAbility() {
        Card card = unlistedCard("Departed Source");
        Permanent source = new Permanent(card);
        source.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        StackEntry ability = TestStackDoor.triggered(card, player2.getId(), "Ability", List.of(), 0, source.getId());
        ability.setSourcePermanentSnapshot(source);
        gd.stack.add(ability);

        assertThat(namesOfferedByScroll()).doesNotContain("Departed Source");
    }

    @Test
    @DisplayName("The names offered leave out the source of an ability of a card exiled face down")
    void offeredNamesLeaveOutFaceDownExiledSourceOfAbility() {
        Card card = unlistedCard("Exiled Source");
        gd.addToExile(player2.getId(), card, java.util.UUID.randomUUID(), true);
        gd.stack.add(TestStackDoor.triggered(card, player2.getId(), "Ability", List.of()));

        assertThat(namesOfferedByScroll()).doesNotContain("Exiled Source");
    }

    @Test
    @DisplayName("The names offered say nothing about a face-down permanent or another player's hand")
    void offeredNamesLeaveOutHiddenCards() {
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent hidden = harness.addToBattlefieldAndReturn(player2, unlistedCard("Hidden Permanent"));
        hidden.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        harness.setHand(player2, List.of(unlistedCard("Hidden Hand Card")));
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .contains("Counterspell").doesNotContain("Hidden Permanent", "Hidden Hand Card");
    }

    @Test
    @DisplayName("Resolving the ability prompts the controller to name a card")
    void resolvingPromptsController() {
        Permanent scroll = addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.ChooseCardNameAtResolutionChoice.class);
        var context = (ChoiceContext.ChooseCardNameAtResolutionChoice) interaction.context();
        assertThat(gd.pendingEffectResolutionEntry.getTargetId()).isEqualTo(player2.getId());
        assertThat(context.sourcePermanentId()).isEqualTo(scroll.getId());
        assertThat(interaction.options()).contains("Cursed Scroll");
        assertThat(scroll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealed card matching the chosen name deals 2 damage to the target player")
    void matchingRevealDeals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Counterspell");

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Revealed card with a different name deals no damage")
    void mismatchedRevealDealsNoDamage() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cursed Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Matching reveal can instead deal the 2 damage to a target creature")
    void matchingRevealDamagesTargetCreature() {
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        Permanent drake = addCreatureReady(player2, new FightingDrake());

        harness.activateAbility(player1, 0, null, drake.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Counterspell");

        assertThat(drake.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty hand reveals nothing and deals no damage")
    void emptyHandDealsNoDamage() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cursed Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The random reveal comes from the ability controller's hand")
    void revealUsesControllerHand() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));
        harness.setHand(player2, List.of(new CursedScroll()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cursed Scroll");

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal target makes the ability fizzle before the name choice")
    void illegalTargetFizzlesBeforeNameChoice() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));
        Permanent drake = addCreatureReady(player2, new FightingDrake());

        harness.activateAbility(player1, 0, null, drake.getId());
        gd.playerBattlefields.get(player2.getId()).remove(drake);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyScroll(Player player) {
        return addCreatureReady(player, new CursedScroll());
    }

    @Test
    @DisplayName("A matching reveal still deals damage after Cursed Scroll leaves the battlefield")
    void abilityResolvesWithoutSource() {
        harness.setLife(player2, 20);
        Permanent scroll = addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scroll);
        gd.playerGraveyards.get(player1.getId()).add(scroll.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Counterspell");

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Counterspell");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Multiple cards with the chosen name guarantee damage without discarding them")
    void duplicateNamesGuaranteeDamage() {
        harness.setLife(player2, 20);
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell(), new Counterspell()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Counterspell");

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The name prompt allows valid card names absent from the game")
    void namePromptIncludesCardsOutsideGame() {
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Counterspell()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction).isNotNull();
        harness.handleListChoice(player1, "Fighting Drake");
    }

    @Test
    @DisplayName("The name prompt does not disclose changes to an opponent's hidden hand")
    void namePromptDoesNotLeakOpponentHand() {
        Permanent scroll = addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of(new Counterspell()));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        List<String> firstOptions = List.copyOf(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options());
        harness.handleListChoice(player1, "Cursed Scroll");

        scroll.untap();
        harness.setHand(player2, List.of(new FightingDrake()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyElementsOf(firstOptions);
        harness.handleListChoice(player1, "Cursed Scroll");
    }

    /** Activates the Scroll above what is on the stack and returns the names its resolution offers. */
    private List<String> namesOfferedByScroll() {
        addReadyScroll(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        return gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options();
    }

    /** A card no catalog lists, so only the cards in the game could offer its name. */
    private static Card unlistedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
