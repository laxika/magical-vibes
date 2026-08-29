package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkoumBattlesingerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may boost all Allies you control")
    void ownAllyEntryBoostsAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, allyCreature());

        harness.setHand(player1, List.of(new AkoumBattlesinger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent battlesinger = findPermanent(player1, "Akoum Battlesinger");
        assertThat(gqs.getEffectivePower(gd, existingAlly)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);
    }

    @Test
    @DisplayName("An Ally entering later triggers the boost")
    void anotherAllyEntryBoostsAllAllies() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, allyCreature());

        harness.setHand(player1, List.of(allyCreature()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, existingAlly)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enteringAlly)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());

        harness.setHand(player1, List.of(allyCreature()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(1);
    }

    @Test
    @DisplayName("The optional boost may be declined")
    void boostMayBeDeclined() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());

        harness.setHand(player1, List.of(allyCreature()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(1);
    }

    private static Card allyCreature() {
        Card card = new Card();
        card.setName("Test Ally");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ALLY));
        return card;
    }
}
