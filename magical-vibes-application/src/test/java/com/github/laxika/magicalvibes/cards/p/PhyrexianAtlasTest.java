package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianAtlas.class})
class PhyrexianAtlasTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color")
    void manaAbilityAddsChosenColor() {
        Permanent atlas = harness.addToBattlefieldAndReturn(player1, new PhyrexianAtlas());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(atlas.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Becoming tapped makes each opponent with three poison counters lose life")
    void becomingTappedMakesPoisonedOpponentLoseLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addToBattlefield(player1, new PhyrexianAtlas());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Becoming tapped does not affect an opponent with fewer than three poison counters")
    void becomingTappedDoesNotAffectOpponentBelowThreshold() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addToBattlefield(player1, new PhyrexianAtlas());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Poison threshold is checked at resolution even when it was not met on tapping")
    void opponentReachingThresholdBeforeResolutionLosesLife() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addToBattlefield(player1, new PhyrexianAtlas());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent below the poison threshold at resolution does not lose life")
    void opponentDroppingBelowThresholdBeforeResolutionDoesNotLoseLife() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addToBattlefield(player1, new PhyrexianAtlas());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only the Atlas that becomes tapped triggers")
    void tappingOneAtlasDoesNotTriggerAnotherAtlas() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player1.getId(), 4);
        gd.playerPoisonCounters.put(player2.getId(), 4);
        harness.addToBattlefield(player1, new PhyrexianAtlas());
        Permanent otherAtlas = harness.addToBattlefieldAndReturn(player1, new PhyrexianAtlas());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(otherAtlas.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent-controlled Atlas makes its opponent lose life")
    void opponentControlledAtlasUsesItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addToBattlefield(player2, new PhyrexianAtlas());
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "WHITE");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
