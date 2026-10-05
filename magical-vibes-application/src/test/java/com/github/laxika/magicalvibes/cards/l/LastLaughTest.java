package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CracklingClub;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PardicLancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastLaugh.class, PardicLancer.class, FieryTemper.class, CracklingClub.class, Opalescence.class, MomentaryBlink.class})
class LastLaughTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent entering a graveyard deals 1 damage to each creature and player")
    void permanentGraveyardTriggerDealsDamageToCreaturesAndPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LastLaugh());
        harness.addToBattlefield(player1, new PardicLancer());
        harness.addToBattlefield(player2, new PardicLancer());

        killWithFieryTemper(player2, player1, "Pardic Lancer");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices itself when the last creature leaves the battlefield")
    void sacrificesWhenNoCreaturesRemain() {
        harness.addToBattlefield(player1, new LastLaugh());
        harness.addToBattlefield(player1, new PardicLancer());

        killWithFieryTemper(player2, player1, "Pardic Lancer");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Last Laugh");
        harness.assertInGraveyard(player1, "Last Laugh");
    }

    @Test
    @DisplayName("A noncreature permanent put into a graveyard also triggers the damage ability")
    void noncreaturePermanentGraveyardTriggerDealsDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LastLaugh());
        harness.addToBattlefield(player1, new PardicLancer());
        harness.addToBattlefield(player1, new CracklingClub());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Crackling Club");
    }

    @Test
    @DisplayName("Does not sacrifice itself while a continuous effect makes it a creature")
    void doesNotSacrificeWhileItIsAnAnimatedCreature() {
        harness.addToBattlefield(player1, new LastLaugh());
        harness.addToBattlefield(player1, new Opalescence());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Last Laugh");
    }

    @Test
    @DisplayName("Sacrificing itself does not trigger its damage ability")
    void itsOwnSacrificeDoesNotDealDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LastLaugh());

        harness.runStateBasedActions();
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Last Laugh");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature controlled by an opponent prevents the sacrifice trigger")
    void opponentCreaturePreventsSacrifice() {
        harness.addToBattlefield(player1, new LastLaugh());
        harness.addToBattlefield(player2, new PardicLancer());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Last Laugh");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature entering after the sacrifice trigger does not stop sacrifice")
    void sacrificesEvenIfCreatureEntersBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new LastLaugh());
        harness.runStateBasedActions();
        harness.enterBattlefieldAndReturn(player2, new PardicLancer());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Last Laugh");
        harness.assertOnBattlefield(player2, "Pardic Lancer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Briefly exiling the only creature during resolution triggers sacrifice")
    void sacrificesWhenOnlyCreatureIsMomentarilyExiled() {
        harness.addToBattlefield(player1, new LastLaugh());
        harness.addToBattlefield(player1, new PardicLancer());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Pardic Lancer"));
        harness.assertOnBattlefield(player1, "Pardic Lancer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Last Laugh");
        harness.assertOnBattlefield(player1, "Pardic Lancer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killWithFieryTemper(Player caster, Player targetPlayer, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new FieryTemper()));
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.addMana(caster, ManaColor.RED, 2);
        UUID targetId = harness.getPermanentId(targetPlayer, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
