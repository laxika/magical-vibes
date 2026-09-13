package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieBoa.class, MournfulZombie.class, UrborgElf.class})
class ZombieBoaTest extends BaseCardTest {

    @Test
    @DisplayName("The ability prompts for a color and destroys a blocker of that color")
    void destroysBlockerOfChosenColor() {
        Permanent boa = addReadyZombieBoa();
        Permanent blocker = addCreatureReady(player2, new MournfulZombie());

        activateAndChoose("BLACK");
        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A blocker of another color is not destroyed")
    void keepsBlockerOfAnotherColor() {
        Permanent boa = addReadyZombieBoa();
        Permanent blocker = addCreatureReady(player2, new MournfulZombie());

        activateAndChoose("RED");
        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent boa = addReadyZombieBoa();
        Permanent blocker = addCreatureReady(player2, new MournfulZombie());

        activateAndChoose("BLACK");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Each blocker matching the chosen color is destroyed")
    void destroysEachMatchingBlocker() {
        Permanent boa = addReadyZombieBoa();
        Permanent blackBlocker1 = addCreatureReady(player2, new MournfulZombie());
        Permanent blackBlocker2 = addCreatureReady(player2, new MournfulZombie());
        Permanent greenBlocker = addCreatureReady(player2, new UrborgElf());

        activateAndChoose("BLACK");
        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(blackBlocker1, blackBlocker2)
                .contains(greenBlocker);
    }

    @Test
    @DisplayName("Separate activations retain their individual color choices")
    void separateActivationsRetainTheirColorChoices() {
        Permanent boa = addReadyZombieBoa();
        Permanent blackBlocker = addCreatureReady(player2, new MournfulZombie());
        Permanent greenBlocker = addCreatureReady(player2, new UrborgElf());

        activateAndChoose("BLACK");
        activateAndChoose("GREEN");
        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(blackBlocker, greenBlocker);
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void onlyActivatesAtSorcerySpeed() {
        addReadyZombieBoa();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyZombieBoa() {
        return addCreatureReady(player1, new ZombieBoa());
    }

    private void activateAndChoose(String color) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, color);
    }
}
