package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkoumBattlesinger.class, StoneworkPuma.class, WalkingAtlas.class})
class AkoumBattlesingerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may boost all Allies you control")
    void ownAllyEntryBoostsAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());

        harness.setHand(player1, List.of(new AkoumBattlesinger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent battlesinger = findPermanent(player1, "Akoum Battlesinger");
        assertThat(gqs.getEffectivePower(gd, existingAlly)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);
    }

    @Test
    @DisplayName("An Ally entering later triggers the boost")
    void anotherAllyEntryBoostsAllAllies() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());

        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, existingAlly)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enteringAlly)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());

        harness.setHand(player1, List.of(new WalkingAtlas()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());

        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
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

        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger your Battlesinger")
    void opponentAllyEntryDoesNotTrigger() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());

        harness.enterBattlefieldAndReturn(player2, new StoneworkPuma());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost excludes non-Allies and opposing Allies and does not increase toughness")
    void boostOnlyAffectsOwnAllyPower() {
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent ownAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());

        harness.setHand(player1, List.of(new AkoumBattlesinger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, nonAlly)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingAlly)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownAlly)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownAlly)).isEqualTo(2);
        Permanent battlesinger = findPermanent(player1, "Akoum Battlesinger");
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, battlesinger)).isEqualTo(1);
    }

    @Test
    @DisplayName("A later Ally does not inherit an already resolved boost")
    void laterAllyDoesNotInheritResolvedBoost() {
        harness.setHand(player1, List.of(new AkoumBattlesinger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent battlesinger = findPermanent(player1, "Akoum Battlesinger");

        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent laterAlly = findPermanent(player1, "Stonework Puma");
        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterAlly)).isEqualTo(2);
    }

    @Test
    @DisplayName("Successive Ally entries produce cumulative boosts")
    void successiveAllyEntriesStackBoosts() {
        Permanent battlesinger = harness.addToBattlefieldAndReturn(player1, new AkoumBattlesinger());
        harness.setHand(player1, List.of(new StoneworkPuma(), new StoneworkPuma()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent firstAlly = findPermanent(player1, "Stonework Puma");

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent secondAlly = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThat(gqs.getEffectivePower(gd, battlesinger)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, firstAlly)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondAlly)).isEqualTo(3);
    }
}
