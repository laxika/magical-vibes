package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimoireOfTheDead.class, DarkthicketWolf.class, MentorOfTheMeek.class, GrafdiggersCage.class})
class GrimoireOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability 1 starts discard-cost choice for any card")
    void ability1StartsDiscardChoice() {
        addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        // Any card should be valid for discard
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a card to discard puts ability on stack and adds study counter on resolution")
    void ability1AddsStudyCounterOnResolution() {
        Permanent grimoire = addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Discard was paid
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Darkthicket Wolf");

        // Resolve the ability
        harness.passBothPriorities();

        assertThat(grimoire.getCounterCount(CounterType.STUDY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 1 can accumulate multiple study counters")
    void ability1AccumulatesCounters() {
        Permanent grimoire = addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(grimoire.getCounterCount(CounterType.STUDY)).isEqualTo(1);

        // Untap for next activation
        grimoire.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(grimoire.getCounterCount(CounterType.STUDY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability 1 without a card in hand")
    void ability1RequiresCardInHand() {
        addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    @DisplayName("Cannot activate ability 1 without enough mana")
    void ability1RequiresMana() {
        addReadyGrimoire();
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability 2 returns all creature cards from all graveyards to battlefield")
    void ability2ReturnsAllCreaturesFromAllGraveyards() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));
        harness.setGraveyard(player2, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Both creatures should be on player1's battlefield
        long creatureCount = countPermanents(player1, "Darkthicket Wolf");
        assertThat(creatureCount).isEqualTo(2);

        // Graveyards should be empty of creatures
        harness.assertNotInGraveyard(player1, "Darkthicket Wolf");
        harness.assertNotInGraveyard(player2, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("Ability 2 sacrifices Grimoire as cost")
    void ability2SacrificesGrimoire() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, 1, null, null);

        // Grimoire should be gone from battlefield (sacrificed as cost)
        harness.assertNotOnBattlefield(player1, "Grimoire of the Dead");
    }

    @Test
    @DisplayName("Returned creatures gain Zombie subtype in addition to their other types")
    void returnedCreaturesGainZombieSubtype() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Darkthicket Wolf");

        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ZOMBIE)).isTrue();
        // Original subtypes preserved
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.WOLF)).isTrue();
    }

    @Test
    @DisplayName("Returned creatures gain black color in addition to their other colors")
    void returnedCreaturesGainBlackColor() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Darkthicket Wolf");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot activate ability 2 without three study counters")
    void ability2RequiresThreeStudyCounters() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Study counters are removed as cost for ability 2")
    void ability2RemovesStudyCounters() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));

        // After activation the permanent is sacrificed, so counters are gone with it
        // But we can verify the ability goes on stack (meaning cost was paid)
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Grafdigger's Cage leaves blocked creatures in their original graveyards")
    void blockedOpponentCreatureStaysInOpponentsGraveyard() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");
        harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");
        harness.assertInGraveyard(player2, "Darkthicket Wolf");
        harness.assertNotInGraveyard(player1, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("A noncreature card can be discarded to pay for a study counter")
    void ability1AcceptsNoncreatureDiscard() {
        Permanent grimoire = addReadyGrimoire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrimoireOfTheDead()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(grimoire.isTapped()).isTrue();
        assertThat(grimoire.getCounterCount(CounterType.STUDY)).isZero();
        harness.assertInGraveyard(player1, "Grimoire of the Dead");
        harness.passBothPriorities();
        assertThat(grimoire.getCounterCount(CounterType.STUDY)).isEqualTo(1);
    }


    @Test
    @DisplayName("Creatures entering together see each other's entry regardless of graveyard order")
    void returnedMentorSeesCreatureReturnedBeforeIt() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of(new DarkthicketWolf(), new MentorOfTheMeek()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkthicket Wolf");
        harness.assertOnBattlefield(player1, "Mentor of the Meek");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Noncreature cards stay in their graveyards")
    void ability2LeavesNoncreatureCardsInGraveyards() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player2, List.of(new DarkthicketWolf(), new GrimoireOfTheDead()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkthicket Wolf");
        harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");
        harness.assertInGraveyard(player2, "Grimoire of the Dead");
        harness.assertInGraveyard(player1, "Grimoire of the Dead");
    }

    @Test
    @DisplayName("Ability 2 can resolve with no creature cards in any graveyard")
    void ability2ResolvesWithEmptyGraveyards() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grimoire of the Dead");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both abilities require an untapped Grimoire")
    void tappedGrimoireCannotActivateEitherAbility() {
        Permanent grimoire = addReadyGrimoire();
        grimoire.setCounterCount(CounterType.STUDY, 3);
        grimoire.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grimoire of the Dead");
        assertThat(grimoire.getCounterCount(CounterType.STUDY)).isEqualTo(3);
    }

    private Permanent addReadyGrimoire() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GrimoireOfTheDead());
        perm.setSummoningSick(false);
        return perm;
    }
}
