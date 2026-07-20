package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StoneGolem;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionOfRhonasTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts the exert may ability")
    void attackPromptsExert() {
        harness.setHand(player1, List.of(new StoneGolem()));
        addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exerting skips the next untap and puts a chosen creature from hand onto the battlefield")
    void exertPutsCreatureAndSkipsUntap() {
        harness.setHand(player1, List.of(new StoneGolem()));
        Permanent champion = addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(champion.getSkipUntapCount()).isGreaterThan(0);
        Permanent golem = findPermanent(player1, "Stone Golem");
        assertThat(golem).isNotNull();
        assertThat(golem.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exerting but declining the card choice leaves the creature in hand while still skipping untap")
    void exertDecliningCardKeepsCreatureInHand() {
        harness.setHand(player1, List.of(new StoneGolem()));
        Permanent champion = addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(champion.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stone Golem"));
    }

    @Test
    @DisplayName("Declining exert skips untap and does not put anything")
    void decliningExertDoesNothing() {
        harness.setHand(player1, List.of(new StoneGolem()));
        Permanent champion = addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(champion.getSkipUntapCount()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only creature cards in hand are offered as choices")
    void offersOnlyCreatureCards() {
        harness.setHand(player1, List.of(new HolyDay(), new Plains(), new StoneGolem()));
        addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
    }

    // ===== Helpers =====

    private Permanent addReadyChampion(Player player) {
        Permanent perm = new Permanent(new ChampionOfRhonas());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
