package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
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

@CardUsed({InnerFlameIgniter.class, WoodlandChangeling.class, NamelessInversion.class})
class InnerFlameIgniterTest extends BaseCardTest {

    @Test
    @DisplayName("Each activation gives +1/+0 to creatures you control; no first strike before the third")
    void pumpsWithoutFirstStrikeBeforeThird() {
        harness.addToBattlefield(player1, new InnerFlameIgniter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.RED, 6);

        activateAndResolve();
        activateAndResolve();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Third resolution grants first strike to creatures you control (plus the +1/+0)")
    void thirdResolutionGrantsFirstStrike() {
        harness.addToBattlefield(player1, new InnerFlameIgniter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.RED, 9);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new InnerFlameIgniter());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.RED, 9);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        assertThat(enemyBears.getPowerModifier()).isEqualTo(0);
        assertThat(enemyBears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fourth resolution pumps new creatures without granting them first strike")
    void fourthResolutionDoesNotGrantFirstStrikeAgain() {
        Permanent igniter = harness.addToBattlefieldAndReturn(player1, new InnerFlameIgniter());
        harness.addMana(player1, ManaColor.RED, 12);

        activateAndResolve();
        activateAndResolve();
        activateAndResolve();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        activateAndResolve();

        assertThat(igniter.getPowerModifier()).isEqualTo(4);
        assertThat(igniter.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(lateCreature.getPowerModifier()).isEqualTo(1);
        assertThat(lateCreature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Separate Igniters count their own ability resolutions")
    void separateIgnitersDoNotShareResolutionCount() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new InnerFlameIgniter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new InnerFlameIgniter());
        harness.addMana(player1, ManaColor.RED, 12);

        activateAndResolve();
        activateAndResolve();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(second.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        activateAndResolve();

        assertThat(first.getPowerModifier()).isEqualTo(4);
        assertThat(second.getPowerModifier()).isEqualTo(4);
        assertThat(first.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(second.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The affected creatures are determined when the ability resolves")
    void queuedActivationsAffectCreaturesPresentAtResolution() {
        Permanent igniter = harness.addToBattlefieldAndReturn(player1, new InnerFlameIgniter());
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(igniter.getPowerModifier()).isZero();
        assertThat(igniter.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(lateCreature.getPowerModifier()).isEqualTo(2);
        assertThat(lateCreature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(lateCreature.getPowerModifier()).isEqualTo(3);
        assertThat(lateCreature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses expire and the resolution count resets for the next turn")
    void bonusesExpireAndResolutionCountResets() {
        Permanent igniter = harness.addToBattlefieldAndReturn(player1, new InnerFlameIgniter());
        harness.addMana(player1, ManaColor.RED, 9);
        activateAndResolve();
        activateAndResolve();
        activateAndResolve();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(igniter.getPowerModifier()).isZero();
        assertThat(igniter.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.addMana(player1, ManaColor.RED, 9);
        activateAndResolve();
        activateAndResolve();
        assertThat(igniter.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        activateAndResolve();
        assertThat(igniter.getPowerModifier()).isEqualTo(3);
        assertThat(igniter.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Queued abilities still grant first strike on their third resolution after the Igniter dies")
    void queuedAbilitiesResolveAfterSourceDies() {
        Permanent igniter = harness.addToBattlefieldAndReturn(player1, new InnerFlameIgniter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.RED, 9);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, igniter.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(igniter);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
