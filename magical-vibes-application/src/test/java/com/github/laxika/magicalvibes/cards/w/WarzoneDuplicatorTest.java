package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarzoneDuplicator.class, GrizzlyBears.class, HillGiant.class, CacklingCounterpart.class})
class WarzoneDuplicatorTest extends BaseCardTest {

    @Test
    void returnsAnOpponentsSmallerCreatureAndConjuresAnAnyColorDuplicate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new WarzoneDuplicator());
        chooseTarget(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.isTokenCard()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(duplicate));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().isToken());
    }

    @Test
    void onlyOffersOpponentCreaturesWithPowerLessThanItsCurrentPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new WarzoneDuplicator());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(opponentCreature.getId());
    }

    @Test
    void prototypePowerRejectsAnEqualPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new WarzoneDuplicator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Warzone Duplicator").getCard().getPower()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void doesNotConjureADuplicateForAToken() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new CacklingCounterpart()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, original.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.enterBattlefieldAndReturn(player1, new WarzoneDuplicator());
        chooseTarget(token);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private void chooseTarget(Permanent target) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
