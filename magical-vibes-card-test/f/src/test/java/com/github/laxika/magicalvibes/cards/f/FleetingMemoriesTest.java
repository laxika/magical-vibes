package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.d.DauntlessCathar;
import com.github.laxika.magicalvibes.cards.e.ExplosiveApparatus;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleetingMemories.class, DauntlessCathar.class, ExplosiveApparatus.class, ThrabenInspector.class})
class FleetingMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and investigates")
    void entersAndInvestigates() {
        harness.setHand(player1, List.of(new FleetingMemories()));
        harness.setLibrary(player1, List.of(new DauntlessCathar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Clue triggers target player milling three cards")
    void sacrificingClueTriggersMill() {
        harness.addToBattlefield(player1, new FleetingMemories());
        Permanent clue = addClueToken(player1);
        harness.setLibrary(player1, List.of(new DauntlessCathar()));
        harness.setLibrary(player2, List.of(new DauntlessCathar(), new DauntlessCathar(), new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not trigger milling")
    void nonClueSacrificeDoesNotTriggerMill() {
        harness.addToBattlefield(player1, new FleetingMemories());
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new ExplosiveApparatus());
        harness.setLibrary(player1, List.of(new DauntlessCathar()));
        harness.setLibrary(player2, List.of(new DauntlessCathar(), new DauntlessCathar(), new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apparatus), 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The controller can target themselves with the mill trigger")
    void canMillController() {
        harness.addToBattlefield(player1, new FleetingMemories());
        Permanent clue = addClueToken(player1);
        harness.setLibrary(player1, List.of(new DauntlessCathar(), new DauntlessCathar(),
                new DauntlessCathar(), new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A player with fewer than three cards mills their remaining library")
    void millsShortLibrary() {
        harness.addToBattlefield(player1, new FleetingMemories());
        Permanent clue = addClueToken(player1);
        harness.setLibrary(player1, List.of(new DauntlessCathar()));
        harness.setLibrary(player2, List.of(new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue does not trigger Fleeting Memories")
    void opponentClueDoesNotTriggerMill() {
        harness.addToBattlefield(player1, new FleetingMemories());
        Permanent clue = addClueToken(player2);
        harness.setLibrary(player2, List.of(new DauntlessCathar(), new DauntlessCathar(),
                new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Fleeting Memories triggers separately for the same sacrificed Clue")
    void multipleCopiesEachMillThree() {
        harness.addToBattlefield(player1, new FleetingMemories());
        harness.addToBattlefield(player1, new FleetingMemories());
        Permanent clue = addClueToken(player1);
        harness.setLibrary(player1, List.of(new DauntlessCathar()));
        harness.setLibrary(player2, List.of(new DauntlessCathar(), new DauntlessCathar(),
                new DauntlessCathar(), new DauntlessCathar(), new DauntlessCathar(),
                new DauntlessCathar(), new DauntlessCathar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addClueToken(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new ThrabenInspector()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castCreature(player, 0);
        resolveAllTriggers();
        return findPermanent(player, "Clue");
    }
}
