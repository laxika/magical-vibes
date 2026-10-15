package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasShade.class, Swamp.class, Forest.class})
class LilianasShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the enter trigger offers only Swamp cards from the library")
    void acceptingOffersOnlySwamps() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(2);
        assertThat(offered).allMatch(c -> c.getName().equals("Swamp"));
    }

    @Test
    @DisplayName("Choosing a Swamp puts it into hand")
    void choosingSwampPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Swamp"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the enter trigger skips the library search")
    void decliningSkipsSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
    }

    @Test
    @DisplayName("Activating the ability gives +1/+1 until end of turn")
    void activatingBoostsUntilEndOfTurn() {
        Permanent shade = addShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The found Swamp is revealed and the remaining library is shuffled")
    void searchRevealsAndShuffles() {
        setupAndCast();
        Swamp swamp = new Swamp();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(swamp, forest));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Swamp"))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find even when a Swamp is available")
    void searchMayFailToFind() {
        setupAndCast();
        Swamp swamp = new Swamp();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(swamp, forest));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(swamp, forest);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes the trigger")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost can be activated while summoning sick and affects only its source")
    void summoningSickShadeCanBoostOnlyItself() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new LilianasShade());
        Permanent otherShade = harness.addToBattlefieldAndReturn(player1, new LilianasShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(otherShade.getEffectivePower()).isEqualTo(1);
        assertThat(otherShade.getEffectiveToughness()).isEqualTo(1);
        assertThat(shade.isTapped()).isFalse();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new LilianasShade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Swamp(), new Forest(), new Swamp(), new LilianasShade()));
    }

    private Permanent addShadeReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianasShade());
        perm.setSummoningSick(false);
        return perm;
    }
}
