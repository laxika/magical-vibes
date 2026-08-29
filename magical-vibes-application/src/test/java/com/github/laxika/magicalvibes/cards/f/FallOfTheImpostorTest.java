package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FallOfTheImpostorTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I puts a +1/+1 counter on the chosen creature")
    void chapterIPutsCounterOnChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallOfTheImpostor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III targets an opponent and exiles a creature tied for greatest power")
    void chapterIIIExilesChosenGreatestPowerCreature() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheImpostor());
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validIds()).containsExactly(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SagaChapterPlayerTarget.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.playerId()).isEqualTo(player1.getId());
        assertThat(creatureChoice.validIds()).containsExactlyInAnyOrder(firstGiant.getId(), secondGiant.getId());

        harness.handlePermanentChosen(player1, firstGiant.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondGiant.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }
}
