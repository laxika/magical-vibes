package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomewardPath.class, GrizzlyBears.class, SolRing.class})
class HomewardPathTest extends BaseCardTest {

    @Test
    void manaAbilityAddsColorlessMana() {
        Permanent path = addReadyPath();

        harness.activateAbility(player1, battlefieldIndex(path), 0, null, null);

        assertThat(path.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void eachPlayerRegainsControlOfTheirOwnedCreatures() {
        Permanent path = addReadyPath();
        Permanent player2Creature = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(player2Creature.getId(), player2.getId());
        Permanent player1Creature = addCreatureReady(player2, new GrizzlyBears());
        gd.stolenCreatures.put(player1Creature.getId(), player1.getId());

        harness.activateAbility(player1, battlefieldIndex(path), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(path, player1Creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player2Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player1Creature);
    }

    private Permanent addReadyPath() {
        return harness.addToBattlefieldAndReturn(player1, new HomewardPath());
    }

    @Test
    void controlAbilityUsesTheStackAndSurvivesItsSourceLeaving() {
        Permanent path = addReadyPath();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.activateAbility(player1, battlefieldIndex(path), 1, null, null);

        assertThat(path.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, path));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(creature.isSummoningSick()).isTrue();
    }

    @Test
    void noncreaturePermanentsStayWithTheirCurrentController() {
        Permanent path = addReadyPath();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), ring,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Test control effect"));

        harness.activateAbility(player1, battlefieldIndex(path), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ring);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ring);
    }

    @Test
    void creaturesAlreadyControlledByTheirOwnersRemainReady() {
        Permanent path = addReadyPath();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(path), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(ownCreature.isSummoningSick()).isFalse();
        assertThat(opposingCreature.isSummoningSick()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
