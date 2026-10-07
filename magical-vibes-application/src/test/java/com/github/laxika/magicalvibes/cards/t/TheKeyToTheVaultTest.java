package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheKeyToTheVault.class, GrizzlyBears.class, Island.class, Forest.class,
        LovestruckBeast.class, HeartsDesire.class})
class TheKeyToTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage looks at that many cards and exiles a chosen nonland card")
    void exilesChosenNonlandCardAfterLookingAtCombatDamageCount() {
        Card chosen = new GrizzlyBears();
        Card land = new Island();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(land, chosen, untouched));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, untouched);
    }

    @Test
    @DisplayName("The exiled card can be cast without paying its mana cost")
    void castsChosenExiledCardForFree() {
        Card chosen = new GrizzlyBears();
        Card land = new Island();
        harness.setLibrary(player1, List.of(chosen, land));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .contains(chosen.getId());
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    @DisplayName("Equip attaches the Key to a creature for two generic and one blue mana")
    void equipsCreature() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new TheKeyToTheVault());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(key.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining exile puts all looked-at cards below the untouched library")
    void mayDeclineToExile() {
        Card nonland = new GrizzlyBears();
        Card land = new Island();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(nonland, land, untouched));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.findExiledCard(nonland.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(nonland, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A land-only selection is returned to the bottom without a cast offer")
    void landOnlyCardsGoToBottom() {
        Card island = new Island();
        Card forest = new Forest();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(island, forest, untouched));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library smaller than the damage amount still allows exiling its nonland card")
    void looksAtEntireShortLibrary() {
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
    }

    @Test
    @DisplayName("An empty library produces no choice and no cards")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The Equipment controller looks at their library when another player's creature deals damage")
    void equipmentControllerGetsTheChoice() {
        Card chosen = new GrizzlyBears();
        Card land = new Island();
        Card otherLibrary = new Forest();
        harness.setLibrary(player1, List.of(chosen, land));
        harness.setLibrary(player2, List.of(otherLibrary));
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent key = harness.addToBattlefieldAndReturn(player1, new TheKeyToTheVault());
        key.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(otherLibrary);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("An exiled adventurer card may be cast as its Adventure for free")
    void offersAdventureSpellForExiledAdventurer() {
        Card chosen = new LovestruckBeast();
        harness.setLibrary(player1, List.of(chosen, new Island()));
        addEquippedAttacker();

        resolveCombat();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Cast Lovestruck Beast", "Cast Heart's Desire");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ListChoiceMade("Cast Heart's Desire"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
    }

    private void addEquippedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent key = addCreatureReady(player1, new TheKeyToTheVault());
        key.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
    }

}
