package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CracklingClub;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
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

@CardUsed({LastLaugh.class, PardicLancer.class, FieryTemper.class, CracklingClub.class, Opalescence.class})
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
