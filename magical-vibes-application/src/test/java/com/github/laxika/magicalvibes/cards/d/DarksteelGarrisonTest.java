package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelGarrison.class, DryadArbor.class, NessianCourser.class})
class DarksteelGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Fortify attaches Darksteel Garrison to a land and grants indestructible")
    void fortifyAttachesToControlledLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, garrison), null, land.getId());
        harness.passBothPriorities();

        assertThat(garrison.getAttachedTo()).isEqualTo(land.getId());
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Fortify cannot target a creature")
    void fortifyCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, garrison), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Fortify cannot target an opponent's land")
    void fortifyCannotTargetOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, garrison), null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Fortify can only be activated during the controller's main phase")
    void fortifyRequiresSorcerySpeed() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, garrison), null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Tapping the fortified land gives a target creature +1/+1")
    void tappingFortifiedLandBoostsTargetCreature() {
        Permanent land = addCreatureReady(player1, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        garrison.setAttachedTo(land.getId());
        Permanent creature = addCreatureReady(player1, new NessianCourser());

        harness.tapPermanent(player1, indexOf(player1, land));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Darksteel Garrison stays on the battlefield when its land leaves")
    void remainsOnBattlefieldWhenFortifiedLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        garrison.setAttachedTo(land.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(garrison);
        assertThat(garrison.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Moving the Garrison transfers indestructible to the new fortified land")
    void movingGarrisonTransfersIndestructible() {
        Permanent oldLand = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent newLand = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        garrison.setAttachedTo(oldLand.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, garrison), null, newLand.getId());
        harness.passBothPriorities();

        assertThat(garrison.getAttachedTo()).isEqualTo(newLand.getId());
        assertThat(gqs.hasKeyword(gd, oldLand, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newLand, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, garrison, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The tap trigger can boost an opponent's creature and survives its source leaving")
    void tapTriggerCanTargetOpponentAndSurvivesSourceLeaving() {
        Permanent land = addCreatureReady(player1, new DryadArbor());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        garrison.setAttachedTo(land.getId());
        Permanent creature = addCreatureReady(player2, new NessianCourser());

        harness.tapPermanent(player1, indexOf(player1, land));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(garrison);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
