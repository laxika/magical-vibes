package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KotoriPilotProdigy.class, AirResponseUnit.class, GrizzlyBears.class, JhoirasFamiliar.class})
class KotoriPilotProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Vehicles you control gain crew 2")
    void grantsCrewTwoToVehiclesYouControl() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        Permanent pilot = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, vehicle), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At the beginning of combat, grants lifelink and vigilance to a target artifact creature you control")
    void grantsLifelinkAndVigilanceAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(artifactCreature.getId());

        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The combat keywords wear off at end of turn")
    void combatKeywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        harness.addToBattlefield(player1, new KotoriPilotProdigy());
        harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonartifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
