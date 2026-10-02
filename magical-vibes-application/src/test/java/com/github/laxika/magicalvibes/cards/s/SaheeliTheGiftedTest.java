package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaheeliTheGifted.class, GrizzlyBears.class, WornPowerstone.class})
class SaheeliTheGiftedTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a Servo artifact creature token")
    void plusOneCreatesServo() {
        Permanent saheeli = addReadySaheeli(4);

        harness.activateAbility(player1, battlefieldIndex(saheeli), 0, null, null);
        harness.passBothPriorities();

        Permanent servo = findPermanents(player1, "Servo").getFirst();
        assertThat(servo.getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, servo)).isTrue();
        assertThat(gqs.isCreature(gd, servo)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 reduces the next spell by one generic mana for each artifact")
    void plusOneGrantsAffinityForArtifactsToNextSpell() {
        Permanent saheeli = addReadySaheeli(4);
        harness.addToBattlefield(player1, new WornPowerstone());

        harness.activateAbility(player1, battlefieldIndex(saheeli), 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("-7 copies each artifact with haste and next-end-step exile")
    void minusSevenCopiesArtifacts() {
        Permanent saheeli = addReadySaheeli(7);
        harness.addToBattlefield(player1, new WornPowerstone());

        harness.activateAbility(player1, battlefieldIndex(saheeli), 2, null, null);
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Worn Powerstone").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(
                        copies.getFirst().getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Worn Powerstone")).hasSize(1);
    }

    private Permanent addReadySaheeli(int loyalty) {
        Permanent saheeli = harness.addToBattlefieldAndReturn(player1, new SaheeliTheGifted());
        saheeli.setCounterCount(CounterType.LOYALTY, loyalty);
        saheeli.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return saheeli;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
