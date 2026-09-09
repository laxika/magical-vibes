package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZelyonSword;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LokiGodOfMischief.class, ProdigalPyromancer.class, ZelyonSword.class, GrizzlyBears.class, Shock.class})
class LokiGodOfMischiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws only once when its controller's abilities target players")
    void drawsOnlyOnceForControllerAbilitiesTargetingPlayers() {
        addReadyProdigalPyromancer(player1);
        addReadyProdigalPyromancer(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws when its controller's ability targets a permanent")
    void drawsForControllerAbilityTargetingPermanent() {
        Permanent sword = addReadySword(player1);
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(sword.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for a targeted spell")
    void doesNotTriggerForTargetedSpell() {
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger for an ability controlled by an opponent")
    void doesNotTriggerForOpponentAbility() {
        harness.addToBattlefield(player1, new LokiGodOfMischief());
        addReadyProdigalPyromancer(player2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private Permanent addReadyProdigalPyromancer(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ProdigalPyromancer());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadySword(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ZelyonSword());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
