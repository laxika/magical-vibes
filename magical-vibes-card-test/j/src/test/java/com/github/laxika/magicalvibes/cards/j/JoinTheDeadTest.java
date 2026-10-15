package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EarthshakerDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoinTheDead.class, EarthshakerDreadmaw.class})
class JoinTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -5/-5 below descend 4")
    void givesMinusFiveMinusFiveBelowDescendFour() {
        Permanent target = addTarget();
        castJoinTheDead(target);

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -10/-10 with four permanent cards in the graveyard")
    void givesMinusTenMinusTenAtDescendFour() {
        harness.setGraveyard(player1, List.of(
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new EarthshakerDreadmaw()));
        Permanent target = addTarget();

        castJoinTheDead(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Nonpermanent cards do not count toward descend 4")
    void nonpermanentCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new JoinTheDead()));
        Permanent target = addTarget();

        castJoinTheDead(target);

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Descend 4 is checked when the spell resolves")
    void descendFourIsCheckedAtResolution() {
        Permanent target = addTarget();
        harness.setHand(player1, List.of(new JoinTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new EarthshakerDreadmaw()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The target controller's graveyard does not enable descend 4")
    void opponentsGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw(),
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw()));
        Permanent target = addTarget();

        castJoinTheDead(target);

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Losing descend 4 before resolution restores the base effect")
    void losingDescendBeforeResolutionUsesBaseEffect() {
        harness.setGraveyard(player1, List.of(
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw(),
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw()));
        Permanent target = addTarget();
        harness.setHand(player1, List.of(new JoinTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of(
                new EarthshakerDreadmaw(), new EarthshakerDreadmaw(), new EarthshakerDreadmaw()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The reduction expires at the end of the turn")
    void reductionExpiresAtEndOfTurn() {
        Permanent target = addTarget();
        castJoinTheDead(target);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());

        castJoinTheDead(target);

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    private Permanent addTarget() {
        return harness.addToBattlefieldAndReturn(player2, new EarthshakerDreadmaw());
    }

    private void castJoinTheDead(Permanent target) {
        harness.setHand(player1, List.of(new JoinTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
