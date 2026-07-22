package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.t.TormentedPariah;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeierReachBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Vildin-Pack Alpha when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new GeierReachBandit());
        Permanent bandit = findPermanent(player1, "Geier Reach Bandit");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bandit.isTransformed()).isTrue();
        assertThat(bandit.getCard().getName()).isEqualTo("Vildin-Pack Alpha");
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bandit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new GeierReachBandit());
        Permanent bandit = findPermanent(player1, "Geier Reach Bandit");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bandit.isTransformed()).isFalse();
        assertThat(bandit.getCard().getName()).isEqualTo("Geier Reach Bandit");
    }

    @Test
    @DisplayName("Vildin-Pack Alpha transforms back when a player cast two or more spells last turn")
    void alphaTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new GeierReachBandit());
        Permanent bandit = findPermanent(player1, "Geier Reach Bandit");

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bandit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bandit.isTransformed()).isFalse();
        assertThat(bandit.getCard().getName()).isEqualTo("Geier Reach Bandit");
    }

    @Test
    @DisplayName("Alpha may transform an entering Werewolf you control")
    void alphaMayTransformEnteringWerewolf() {
        harness.addToBattlefield(player1, new GeierReachBandit());
        Permanent bandit = findPermanent(player1, "Geier Reach Bandit");

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bandit.getCard().getName()).isEqualTo("Vildin-Pack Alpha");

        harness.setHand(player1, List.of(new TormentedPariah()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Tormented Pariah

        harness.passBothPriorities(); // resolve Alpha's may-transform trigger → prompt
        harness.handleMayAbilityChosen(player1, true);

        Permanent pariah = findPermanent(player1, "Rampaging Werewolf");
        assertThat(pariah).isNotNull();
        assertThat(pariah.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining Alpha's trigger leaves the entering Werewolf untransformed")
    void decliningAlphaLeaveWerewolfUntransformed() {
        harness.addToBattlefield(player1, new GeierReachBandit());
        Permanent bandit = findPermanent(player1, "Geier Reach Bandit");

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TormentedPariah()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent pariah = findPermanent(player1, "Tormented Pariah");
        assertThat(pariah).isNotNull();
        assertThat(pariah.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Alpha does not trigger for a non-Werewolf entering")
    void alphaDoesNotTriggerForNonWerewolf() {
        harness.addToBattlefield(player1, new GeierReachBandit());
        Permanent bandit = findPermanent(player1, "Geier Reach Bandit");

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Air Elemental").isTransformed()).isFalse();
    }
}
