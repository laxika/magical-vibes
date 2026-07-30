package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XathridGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Petrification adds the counter, defender, artifact type and colorlessness")
    void petrifiesTargetCreature() {
        addReadyGorgon(player1);
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
        addReadyGorgon(player1);
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
        addReadyGorgon(player1);
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
        addReadyGorgon(player1);
        Permanent pacifism = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(pacifism);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pacifism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activatePetrify(Permanent target) {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyGorgon(Player player) {
        Permanent perm = new Permanent(new XathridGorgon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
