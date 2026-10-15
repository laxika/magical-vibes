package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HapatrasMark;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionOfRhonas.class, Colossapede.class, HapatrasMark.class, CascadingCataracts.class})
class ChampionOfRhonasTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts the exert may ability")
    void attackPromptsExert() {
        harness.setHand(player1, List.of(new Colossapede()));
        addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exerting skips the next untap and puts a chosen creature from hand onto the battlefield")
    void exertPutsCreatureAndSkipsUntap() {
        harness.setHand(player1, List.of(new Colossapede()));
        Permanent champion = addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(champion.getSkipUntapCount()).isGreaterThan(0);
        Permanent creature = findPermanent(player1, "Colossapede");
        assertThat(creature).isNotNull();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exerting but declining the card choice leaves the creature in hand while still skipping untap")
    void exertDecliningCardKeepsCreatureInHand() {
        harness.setHand(player1, List.of(new Colossapede()));
        Permanent champion = addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(champion.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Colossapede");
    }

    @Test
    @DisplayName("Declining exert allows normal untapping and does not put anything")
    void decliningExertDoesNothing() {
        harness.setHand(player1, List.of(new Colossapede()));
        Permanent champion = addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(champion.getSkipUntapCount()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only creature cards in hand are offered as choices")
    void offersOnlyCreatureCards() {
        harness.setHand(player1, List.of(new HapatrasMark(), new CascadingCataracts(), new Colossapede()));
        addReadyChampion(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
    }

    @Test
    @DisplayName("Exert is paid before players can respond to the creature-putting trigger")
    void exertIsPaidBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new Colossapede()));
        Permanent champion = addReadyChampion(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(champion.getSkipUntapCount()).isPositive();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
            harness.assertNotOnBattlefield(player1, "Colossapede");
        });
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a different controller's untap step")
    void exertDoesNotFreezeNewControllerUntap() {
        harness.setHand(player1, List.of(new Colossapede()));
        Permanent champion = addReadyChampion(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, -1);
        });

        gd.playerBattlefields.get(player1.getId()).remove(champion);
        gd.playerBattlefields.get(player2.getId()).add(champion);
        champion.setAttacking(false);
        champion.tap();
        harness.performUntapStep(player2);

        assertThat(champion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert with no creature in hand still skips exactly the next untap")
    void exertWithEmptyHandSkipsOneUntap() {
        harness.setHand(player1, List.of());
        Permanent champion = addReadyChampion(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        });

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(champion.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(champion.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(champion.isTapped()).isFalse();
    }

    private Permanent addReadyChampion(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChampionOfRhonas());
        perm.setSummoningSick(false);
        return perm;
    }
}
