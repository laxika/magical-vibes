package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JuneBountyHunter.class, YuyanArchers.class})
class JuneBountyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("June can be blocked before her controller draws two cards")
    void isBlockableBeforeTwoDraws() {
        Permanent june = addJune();

        assertThat(gqs.hasCantBeBlocked(gd, june)).isFalse();
    }

    @Test
    @DisplayName("June cannot be blocked after her controller draws two cards")
    void becomesUnblockableAfterTwoDraws() {
        Permanent june = addJune();
        harness.setLibrary(player1, List.of(new YuyanArchers(), new YuyanArchers()));

        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, june)).isFalse();

        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, june)).isTrue();
    }

    @Test
    @DisplayName("An opponent's draws do not make June unblockable")
    void opponentDrawsDoNotCount() {
        Permanent june = addJune();
        harness.setLibrary(player2, List.of(new YuyanArchers(), new YuyanArchers()));

        draw(player2);
        draw(player2);

        assertThat(gqs.hasCantBeBlocked(gd, june)).isFalse();
    }

    @Test
    @DisplayName("Cards drawn before June enters still make her unblockable")
    void drawsBeforeEnteringCount() {
        harness.setLibrary(player1, List.of(new YuyanArchers(), new YuyanArchers(), new YuyanArchers()));
        draw(player1);
        draw(player1);

        Permanent june = addJune();
        assertThat(gqs.hasCantBeBlocked(gd, june)).isTrue();

        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, june)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another creature creates a Clue token")
    void sacrificesAnotherCreatureAndCreatesClue() {
        Permanent june = addJune();
        harness.addToBattlefield(player1, new YuyanArchers());
        prepareAbility();

        harness.activateAbility(player1, indexOf(player1, june), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(june);
        harness.assertInGraveyard(player1, "Yuyan Archers");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("June's ability cannot be activated during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        Permanent june = addJune();
        harness.addToBattlefield(player1, new YuyanArchers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, june), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("June cannot sacrifice herself when no other creature is controlled")
    void cannotSacrificeHerself() {
        Permanent june = addJune();
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, june), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(june);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Creating a Clue with June is not investigating")
    void creatingClueDoesNotInvestigate() {
        Permanent june = addJune();
        harness.addToBattlefield(player1, new YuyanArchers());
        prepareAbility();

        harness.activateAbility(player1, indexOf(player1, june), 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playersWhoInvestigatedThisTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("June can create a Clue during combat and its ability draws a card")
    void clueCanBeCreatedDuringCombatAndSacrificedToDraw() {
        Permanent june = addJune();
        harness.addToBattlefield(player1, new YuyanArchers());
        harness.setLibrary(player1, List.of(new YuyanArchers()));
        prepareAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, indexOf(player1, june), 0, null, null);
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, clue), 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    private Permanent addJune() {
        return addCreatureReady(player1, new JuneBountyHunter());
    }

    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
