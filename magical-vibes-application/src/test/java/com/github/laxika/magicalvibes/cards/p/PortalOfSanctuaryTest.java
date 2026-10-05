package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AncestralBlade;
import com.github.laxika.magicalvibes.cards.f.FeralInvocation;
import com.github.laxika.magicalvibes.cards.f.FuneralMarch;
import com.github.laxika.magicalvibes.cards.m.MammothSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortalOfSanctuary.class, MammothSpider.class, Pacifism.class, FeralInvocation.class,
        AncestralBlade.class, FuneralMarch.class})
class PortalOfSanctuaryTest extends BaseCardTest {

    @Test
    void canActivateDuringOwnEndStepWithoutAuras() {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new PortalOfSanctuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(portal.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mammoth Spider");
        harness.assertNotOnBattlefield(player1, "Mammoth Spider");
        harness.assertOnBattlefield(player1, "Portal of Sanctuary");
    }

    @Test
    void leavesEquipmentAndUnrelatedAurasOnBattlefield() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AncestralBlade());
        equipment.setAttachedTo(creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(otherCreature.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature, equipment);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(otherCreature.getId());
    }

    @Test
    void simultaneousReturnTriggersAttachedFuneralMarch() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FuneralMarch());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(aura.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(victim.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(victim);
    }

    @Test
    @DisplayName("Returns the target creature and every attached Aura to their owners' hands")
    void returnsCreatureAndAllAttachedAuras() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        ownAura.setAttachedTo(bears.getId());

        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new FeralInvocation());
        opponentAura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mammoth Spider");
        harness.assertInHand(player1, "Mammoth Spider");
        harness.assertInHand(player1, "Pacifism");
        harness.assertInHand(player2, "Feral Invocation");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new PortalOfSanctuary());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new MammothSpider());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MammothSpider());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }
}
