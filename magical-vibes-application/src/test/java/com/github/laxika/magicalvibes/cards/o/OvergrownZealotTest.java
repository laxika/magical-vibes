package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PyrotechnicPerformer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvergrownZealot.class, PyrotechnicPerformer.class})
class OvergrownZealotTest extends BaseCardTest {

    @Test
    void firstAbilityAddsOneManaOfTheChosenColor() {
        addReadyZealot();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void secondAbilityAddsTwoManaOfTheChosenColorForTurningPermanentsFaceUp() {
        addReadyZealot();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void secondAbilityCanPayToTurnAPermanentFaceUp() {
        Permanent zealot = addReadyZealot();
        harness.setHand(player1, List.of(new PyrotechnicPerformer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent performer = findPermanent(player1, "Pyrotechnic Performer");
        int zealotIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zealot);
        harness.activateAbility(player1, zealotIndex, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(performer));

        assertThat(performer.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void secondAbilityCannotPayForAMorphCost() {
        addReadyZealot();
        harness.setHand(player1, List.of(new PyrotechnicPerformer()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getTurnPermanentsFaceUpMana(ManaColor.RED)).isEqualTo(2);
    }

    private Permanent addReadyZealot() {
        return addCreatureReady(player1, new OvergrownZealot());
    }
}
