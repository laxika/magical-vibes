package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BriarbridgePatrolTest extends BaseCardTest {

    @Test
    void damageToCreatureCreatesAClue() {
        Permanent patrol = addCreatureReady(player1, new BriarbridgePatrol());
        patrol.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void threeCluesSacrificedPutCreatureFromHandOntoBattlefieldAtEndStep() {
        addCreatureReady(player1, new BriarbridgePatrol());
        addClues(player1, 3);
        sacrificeClues(3);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void fewerThanThreeCluesDoNotPutCreatureFromHandOntoBattlefield() {
        addCreatureReady(player1, new BriarbridgePatrol());
        addClues(player1, 2);
        sacrificeClues(2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private void addClues(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card clueCard = new Card();
            clueCard.setName("Clue");
            clueCard.setType(CardType.ARTIFACT);
            clueCard.setManaCost("");
            clueCard.setToken(true);
            clueCard.setSubtypes(List.of(CardSubtype.CLUE));
            clueCard.addActivatedAbility(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                    "{2}, Sacrifice this token: Draw a card."
            ));
            Permanent clue = new Permanent(clueCard);
            clue.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(clue);
        }
    }

    private void sacrificeClues(int count) {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        for (int i = 0; i < count; i++) {
            int clueIndex = -1;
            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            for (int j = 0; j < battlefield.size(); j++) {
                if (battlefield.get(j).getCard().getName().equals("Clue")) {
                    clueIndex = j;
                    break;
                }
            }
            assertThat(clueIndex).isGreaterThanOrEqualTo(0);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, clueIndex, null, null);
            harness.passBothPriorities();
            resolveAllTriggers();
        }
    }
}
