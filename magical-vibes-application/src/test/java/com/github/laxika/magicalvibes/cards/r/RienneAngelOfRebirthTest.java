package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.cards.s.Scuttlemutt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RienneAngelOfRebirth.class, DoomBlade.class, GrizzlyBears.class, WoollyThoctar.class,
        Scuttlemutt.class})
class RienneAngelOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other multicolored creatures you control")
    void boostsOtherMulticoloredCreatures() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        harness.addToBattlefield(player1, new WoollyThoctar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent rienne = findPermanent(player1, "Rienne, Angel of Rebirth");
        Permanent woollyThoctar = findPermanent(player1, "Woolly Thoctar");
        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, rienne)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, woollyThoctar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, grizzlyBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns another multicolored creature to its owner's hand at the next end step")
    void returnsMulticoloredCreatureAtNextEndStep() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        harness.addToBattlefield(player1, new WoollyThoctar());
        UUID woollyThoctarId = harness.getPermanentId(player1, "Woolly Thoctar");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, woollyThoctarId);

        harness.assertInGraveyard(player1, "Woolly Thoctar");
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class)).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertInGraveyard(player1, "Woolly Thoctar");
        harness.assertNotInHand(player1, "Woolly Thoctar");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Woolly Thoctar");
        harness.assertNotInGraveyard(player1, "Woolly Thoctar");
    }

    @Test
    @DisplayName("Does not return a monocolored creature")
    void doesNotReturnMonocoloredCreature() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID grizzlyBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, grizzlyBearsId);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotBoostOpponentsMulticoloredCreatures() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoollyThoctar());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void doesNotReturnItself() {
        Permanent rienne = harness.addToBattlefieldAndReturn(player1, new RienneAngelOfRebirth());
        destroyWithDoomBlade(rienne.getId());

        harness.assertInGraveyard(player1, "Rienne, Angel of Rebirth");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class)).isEmpty();
    }

    @Test
    void returnsCreatureThatBecameMulticolored() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        Permanent mutt = harness.addToBattlefieldAndReturn(player1, new Scuttlemutt());
        mutt.setSummoningSick(false);
        harness.activateAbility(player1, 1, 1, null, mutt.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectivePower(gd, mutt)).isEqualTo(3);

        destroyWithDoomBlade(mutt.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Scuttlemutt");
        harness.assertNotInGraveyard(player1, "Scuttlemutt");
    }

    @Test
    void doesNotReturnCreatureThatBecameMonocolored() {
        harness.addToBattlefield(player1, new RienneAngelOfRebirth());
        Permanent mutt = harness.addToBattlefieldAndReturn(player1, new Scuttlemutt());
        mutt.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        destroyWithDoomBlade(creature.getId());

        harness.assertInGraveyard(player1, "Woolly Thoctar");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class)).isEmpty();
    }

    private void destroyWithDoomBlade(UUID targetId) {
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
