package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortalOfSanctuary.class, TrainedArmodon.class, Pacifism.class, GiantStrength.class})
class PortalOfSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and every attached Aura to their owners' hands")
    void returnsCreatureAndAllAttachedAuras() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        ownAura.setAttachedTo(bears.getId());

        Permanent opponentAura = new Permanent(new GiantStrength());
        opponentAura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(opponentAura);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Trained Armodon");
        harness.assertInHand(player1, "Trained Armodon");
        harness.assertInHand(player1, "Pacifism");
        harness.assertInHand(player2, "Giant Strength");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot be activated during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }
}
