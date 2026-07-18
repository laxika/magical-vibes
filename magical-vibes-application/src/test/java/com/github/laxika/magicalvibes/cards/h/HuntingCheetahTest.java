package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HuntingCheetahTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setupLibrary(int forests) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < forests; i++) {
            deck.add(new Forest());
        }
        deck.add(new GrizzlyBears());
        deck.add(new GrizzlyBears());
    }

    @Test
    @DisplayName("Dealing combat damage to a player presents the may prompt")
    void combatDamagePresentsMayChoice() {
        Permanent cheetah = addReadyCreature(player1, new HuntingCheetah());
        cheetah.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may prompt searches the library for Forest cards only")
    void acceptingInitiatesForestSearch() {
        Permanent cheetah = addReadyCreature(player1, new HuntingCheetah());
        cheetah.setAttacking(true);
        setupLibrary(2);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Forest puts it into hand")
    void choosingForestPutsItIntoHand() {
        Permanent cheetah = addReadyCreature(player1, new HuntingCheetah());
        cheetah.setAttacking(true);
        setupLibrary(2);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining the may prompt does not search")
    void decliningSkipsSearch() {
        Permanent cheetah = addReadyCreature(player1, new HuntingCheetah());
        cheetah.setAttacking(true);
        setupLibrary(2);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when blocked and no damage reaches the player")
    void noTriggerWhenBlocked() {
        Permanent cheetah = addReadyCreature(player1, new HuntingCheetah());
        cheetah.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
