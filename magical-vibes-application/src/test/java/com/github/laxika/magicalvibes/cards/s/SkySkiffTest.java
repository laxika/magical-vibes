package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkySkiff.class, KujarSeedsculptor.class, ConsulateSkygate.class})
class SkySkiffTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent skiff = addSkiffReady(player1);

        assertThat(gqs.isCreature(gd, skiff)).isFalse();
    }

    @Test
    void crewAnimatesSkiffAndTapsCrew() {
        Permanent skiff = addSkiffReady(player1);
        Permanent crew = addCreatureReady(player1, new KujarSeedsculptor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skiff)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addSkiffReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent skiff = addSkiffReady(player1);
        addCreatureReady(player1, new KujarSeedsculptor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, skiff)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skiff)).isFalse();
    }

    @Test
    void crewCostIsPaidBeforeAnimationResolves() {
        Permanent skiff = addSkiffReady(player1);
        Permanent crew = addCreatureReady(player1, new KujarSeedsculptor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, skiff)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, skiff)).isTrue();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent skiff = addSkiffReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new KujarSeedsculptor());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, skiff)).isTrue();
    }

    @Test
    void tappedVehicleCanBeCrewedWithoutUntappingIt() {
        Permanent skiff = addSkiffReady(player1);
        skiff.tap();
        addCreatureReady(player1, new KujarSeedsculptor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skiff)).isTrue();
        assertThat(skiff.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithTappedCreature() {
        addSkiffReady(player1);
        Permanent crew = addCreatureReady(player1, new KujarSeedsculptor());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void cannotCrewWithOpponentsCreature() {
        addSkiffReady(player1);
        Permanent crew = addCreatureReady(player2, new KujarSeedsculptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    void zeroPowerCreatureCannotPayCrewOne() {
        addSkiffReady(player1);
        Permanent crew = addCreatureReady(player1, new ConsulateSkygate());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    void animatedSkiffCannotCrewItself() {
        Permanent skiff = addSkiffReady(player1);
        addCreatureReady(player1, new KujarSeedsculptor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(skiff.isTapped()).isFalse();
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addSkiffReady(player1);
        addCreatureReady(player1, new KujarSeedsculptor());
        addCreatureReady(player2, new KujarSeedsculptor());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureWithReachCanBlockCrewedSkiff() {
        Permanent skiff = addSkiffReady(player1);
        addCreatureReady(player1, new KujarSeedsculptor());
        Permanent blocker = addCreatureReady(player2, new ConsulateSkygate());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skiff);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(player2.getLife()).isEqualTo(20);
    }

    private Permanent addSkiffReady(Player player) {
        return addCreatureReady(player, new SkySkiff());
    }
}
