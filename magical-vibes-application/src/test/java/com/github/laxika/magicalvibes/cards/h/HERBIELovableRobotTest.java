package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({HERBIELovableRobot.class, BurstOfStrength.class, GrizzlyBears.class})
class HERBIELovableRobotTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils at beginning of combat after casting a noncreature spell")
    void surveilsAfterCastingNoncreatureSpell() {
        Permanent herbie = addReadyHerbie();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, herbie.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil when only a creature spell was cast")
    void doesNotSurveilAfterCastingCreatureSpell() {
        addReadyHerbie();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("The mana abilities add colorless or one mana of any color")
    void manaAbilitiesProduceMana() {
        Permanent herbie = addReadyHerbie();
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        herbie.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    private Permanent addReadyHerbie() {
        Permanent herbie = harness.addToBattlefieldAndReturn(player1, new HERBIELovableRobot());
        herbie.setSummoningSick(false);
        return herbie;
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
