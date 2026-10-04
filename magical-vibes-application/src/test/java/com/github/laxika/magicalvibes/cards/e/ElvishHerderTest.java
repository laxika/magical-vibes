package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Rescind;
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

@CardUsed({ElvishHerder.class, BullHippo.class, Forest.class, Rescind.class})
class ElvishHerderTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants trample to target creature")
    void grantsTrample() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = addCreatureReady(player1, new BullHippo());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability can target Elvish Herder itself")
    void grantsTrampleToItself() {
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, herder.getId());
        harness.passBothPriorities();

        assertThat(herder.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability can target a creature an opponent controls")
    void grantsTrampleToOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = addCreatureReady(player2, new BullHippo());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOff() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = addCreatureReady(player1, new BullHippo());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = addCreatureReady(player1, new BullHippo());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Tapped, summoning-sick Herder can activate repeatedly for separate creatures")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        herder.setSummoningSick(true);
        herder.setTapped(true);
        Permanent first = addCreatureReady(player1, new BullHippo());
        Permanent second = addCreatureReady(player2, new BullHippo());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(herder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves even when Herder leaves the battlefield in response")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = addCreatureReady(player1, new BullHippo());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Rescind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, herder.getId());
        harness.assertNotOnBattlefield(player1, "Elvish Herder");
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability does not grant trample if its target leaves in response")
    void doesNotGrantTrampleToDepartedTarget() {
        harness.addToBattlefieldAndReturn(player1, new ElvishHerder());
        Permanent target = addCreatureReady(player1, new BullHippo());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Rescind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bull Hippo");
        harness.assertInHand(player1, "Bull Hippo");
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
