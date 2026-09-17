package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeizeTheDay.class, WoodlandDruid.class, Plains.class})
class SeizeTheDayTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target creature and creates an additional combat and main phase")
    void untapsTargetCreatureAndCreatesAdditionalPhases() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();

        castFromPostcombatMain(creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("From precombat main, returns to the regular combat after the added main phase")
    void precombatCastReturnsToRegularCombatAfterAdditionalMainPhase() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Can target and untap a creature controlled by an opponent")
    void canTargetAndUntapOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        creature.tap();

        castFromPostcombatMain(creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only a creature can be targeted")
    void onlyCreatureCanBeTargeted() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExilesSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();
        harness.setGraveyard(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Seize the Day");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seize the Day"));
    }

    private void castFromPostcombatMain(java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetId);
    }
}
