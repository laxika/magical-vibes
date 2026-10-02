package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BasiliskCollar;
import com.github.laxika.magicalvibes.cards.p.PulseTracker;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnowonTheRuinSage.class, WalkingAtlas.class, PulseTracker.class, BasiliskCollar.class})
class AnowonTheRuinSageTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep each player sacrifices a non-Vampire creature")
    void eachPlayerSacrificesNonVampireCreature() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent player1Vampire = harness.addToBattlefieldAndReturn(player1, new PulseTracker());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        Permanent player2Vampire = harness.addToBattlefieldAndReturn(player2, new PulseTracker());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(player1Creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(player2Creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(player1Vampire.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(player2Vampire.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The player chooses which non-Vampire creature to sacrifice")
    void playerChoosesNonVampireCreature() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Noncreatures and Vampires are not eligible")
    void noncreaturesAndVampiresAreNotEligible() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new PulseTracker());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new BasiliskCollar());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(vampire, noncreature);
    }

    @Test
    @DisplayName("Both players choose before any creatures are sacrificed")
    void sacrificesWaitForBothPlayersChoices() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent firstChoice = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent firstSurvivor = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent secondChoice = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        Permanent secondSurvivor = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstChoice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstChoice, firstSurvivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondChoice, secondSurvivor);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMultiplePermanentsChosen(player2, List.of(secondChoice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstSurvivor).doesNotContain(firstChoice);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(secondSurvivor).doesNotContain(secondChoice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstChoice.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondChoice.getCard());
    }

    @Test
    @DisplayName("An automatic sacrifice waits until the other player chooses")
    void automaticSacrificeWaitsForOtherPlayerChoice() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent automaticChoice = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(automaticChoice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen, survivor);
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(automaticChoice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(automaticChoice.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen.getCard());
    }

    @Test
    @DisplayName("Player two chooses first during their own upkeep")
    void playerTwoControllerChoosesFirst() {
        harness.addToBattlefield(player2, new AnowonTheRuinSage());
        Permanent firstChoice = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        Permanent firstSurvivor = harness.addToBattlefieldAndReturn(player2, new WalkingAtlas());
        Permanent secondChoice = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());
        Permanent secondSurvivor = harness.addToBattlefieldAndReturn(player1, new WalkingAtlas());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(firstChoice.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstChoice, firstSurvivor);
        harness.handleMultiplePermanentsChosen(player1, List.of(secondChoice.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(firstSurvivor).doesNotContain(firstChoice);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(secondSurvivor).doesNotContain(secondChoice);
    }
}
