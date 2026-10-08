package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SilentBladeOni;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VelaTheNightClad.class, SilentBladeOni.class, TurnToFrog.class})
class VelaTheNightCladTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have intimidate")
    void grantsIntimidateToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new VelaTheNightClad());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SilentBladeOni());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SilentBladeOni());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.INTIMIDATE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Each opponent loses 1 life when another creature you control leaves")
    void anotherCreatureLeavingCausesEachOpponentToLoseLife() {
        harness.addToBattlefield(player1, new VelaTheNightClad());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SilentBladeOni());
        int opponentLifeBefore = gd.getLife(player2.getId());

        leaveBattlefield(ally);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Each opponent loses 1 life when Vela leaves")
    void sourceLeavingCausesEachOpponentToLoseLife() {
        Permanent vela = harness.addToBattlefieldAndReturn(player1, new VelaTheNightClad());
        int opponentLifeBefore = gd.getLife(player2.getId());

        leaveBattlefield(vela);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Returning an ally to hand triggers life loss without it dying")
    void returningAllyToHandTriggersLifeLoss() {
        harness.addToBattlefield(player1, new VelaTheNightClad());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SilentBladeOni());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ally));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Exiling Vela triggers life loss and removes the intimidate grant")
    void exilingVelaTriggersLifeLossAndEndsGrant() {
        Permanent vela = harness.addToBattlefieldAndReturn(player1, new VelaTheNightClad());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SilentBladeOni());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, vela));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature leaving does not trigger Vela")
    void opponentCreatureLeavingDoesNotCauseLifeLoss() {
        harness.addToBattlefield(player1, new VelaTheNightClad());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SilentBladeOni());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());

        leaveBattlefield(opponentCreature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Vela sees every allied creature that dies simultaneously with it")
    void simultaneousDeathsTriggerForVelaAndEveryAlly() {
        Permanent vela = harness.addToBattlefieldAndReturn(player1, new VelaTheNightClad());
        Permanent firstAlly = harness.addToBattlefieldAndReturn(player1, new SilentBladeOni());
        Permanent secondAlly = harness.addToBattlefieldAndReturn(player1, new SilentBladeOni());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        vela.setMarkedDamage(4);
        firstAlly.setMarkedDamage(5);
        secondAlly.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vela, firstAlly, secondAlly);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Vela does not trigger for its own departure after losing all abilities")
    void noSelfLeavesTriggerAfterLosingAllAbilities() {
        Permanent vela = harness.addToBattlefieldAndReturn(player1, new VelaTheNightClad());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, vela.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        leaveBattlefield(vela);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
