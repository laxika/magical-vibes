package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({DarksteelGarrison.class, Mountain.class, GrizzlyBears.class})
class DarksteelGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Fortify attaches Darksteel Garrison to a land and grants indestructible")
    void fortifyAttachesToControlledLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, garrison), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Tapping the fortified land gives a target creature +1/+1")
    void tappingFortifiedLandBoostsTargetCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        garrison.setAttachedTo(land.getId());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.tapPermanent(player1, indexOf(player1, land));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Darksteel Garrison stays on the battlefield when its land leaves")
    void remainsOnBattlefieldWhenFortifiedLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        garrison.setAttachedTo(land.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(garrison);
        assertThat(garrison.getAttachedTo()).isNull();
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
