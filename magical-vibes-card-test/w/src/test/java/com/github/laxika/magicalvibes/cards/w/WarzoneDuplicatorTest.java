package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
        assertThat(duplicate.isConjured()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(duplicate));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().isConjured());
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
        harness.addMana(player2, ManaColor.BLUE, 2);
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

    @Test
    @DisplayName("A duplicate of a returned face-down creature has only its face-down values")
    void duplicateOfAFaceDownCreatureIsANamelessTwoTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.enterBattlefieldAndReturn(player1, new WarzoneDuplicator());
        chooseTarget(target);

        assertThat(gd.playerHands.get(player1.getId())).filteredOn(Card::isConjured).singleElement().satisfies(duplicate -> {
            assertThat(duplicate.getName()).isEmpty();
            assertThat(duplicate.getPower()).isEqualTo(2);
            assertThat(duplicate.getToughness()).isEqualTo(2);
        });
    }

    private void chooseTarget(Permanent target) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The shared log names the player who gets the duplicate, not \"your hand\"")
    void logNamesThePlayerWhoGetsTheDuplicate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new WarzoneDuplicator());
        chooseTarget(target);

        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains("your hand"));
        assertThat(gd.gameLog).anyMatch(line -> line.plainText().equals(
                player1.getUsername() + " conjures a duplicate of Grizzly Bears into their hand."));
    }
}
