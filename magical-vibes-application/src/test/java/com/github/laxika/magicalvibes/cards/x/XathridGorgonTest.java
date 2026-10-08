package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XathridGorgon.class, GrizzlyBears.class, LlanowarElves.class, Pacifism.class})
class XathridGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Petrification adds the counter, defender, artifact type and colorlessness")
    void petrifiesTargetCreature() {
        addCreatureReady(player1, new XathridGorgon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activatePetrify(target);

        assertThat(target.getCounterCount(CounterType.PETRIFICATION)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
    }

    @Test
    @DisplayName("The petrification effects last indefinitely and survive end of turn")
    void petrificationPersistsPastEndOfTurn() {
        addCreatureReady(player1, new XathridGorgon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activatePetrify(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
        assertThat(target.getCounterCount(CounterType.PETRIFICATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("A petrified creature's activated abilities can't be activated")
    void petrifiedCreatureCannotActivateAbilities() {
        addCreatureReady(player1, new XathridGorgon());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        assertThat(gqs.canActivateManaAbility(gd, elves)).isTrue();

        activatePetrify(elves);

        assertThat(gqs.canActivateManaAbility(gd, elves)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new XathridGorgon());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pacifism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activatePetrify(Permanent target) {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    @Test
    void removingCounterDoesNotUndoPetrification() {
        addCreatureReady(player1, new XathridGorgon());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        activatePetrify(target);

        target.setCounterCount(CounterType.PETRIFICATION, 0);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
        assertThat(gqs.canActivateManaAbility(gd, target)).isFalse();
    }

    @Test
    void effectsPersistWhenGorgonLeavesBattlefield() {
        Permanent gorgon = addCreatureReady(player1, new XathridGorgon());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        activatePetrify(target);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gorgon);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
        assertThat(gqs.canActivateManaAbility(gd, target)).isFalse();
    }

    @Test
    void canPetrifyItselfAndCannotActivateAgain() {
        Permanent gorgon = addCreatureReady(player1, new XathridGorgon());
        activatePetrify(gorgon);

        assertThat(gorgon.isTapped()).isTrue();
        assertThat(gorgon.getCounterCount(CounterType.PETRIFICATION)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.isArtifact(gd, gorgon)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, gorgon)).isEmpty();

        gorgon.setTapped(false);
        harness.addMana(player1, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gorgon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new XathridGorgon());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gorgon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gorgon.getCounterCount(CounterType.PETRIFICATION)).isZero();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        Permanent gorgon = addCreatureReady(player1, new XathridGorgon());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gorgon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gorgon.isTapped()).isFalse();
    }

    @Test
    void petrifiedCreatureCannotAttack() {
        addCreatureReady(player1, new XathridGorgon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        activatePetrify(target);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchDestroysCreatureWithMoreToughnessThanDamage() {
        Permanent attacker = addCreatureReady(player1, new XathridGorgon());
        Permanent blocker = addCreatureReady(player2, new XathridGorgon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Xathrid Gorgon");
        harness.assertInGraveyard(player2, "Xathrid Gorgon");
    }
}
