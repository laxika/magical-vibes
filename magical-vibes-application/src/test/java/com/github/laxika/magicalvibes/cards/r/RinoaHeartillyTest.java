package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RinoaHeartilly.class, GrizzlyBears.class})
class RinoaHeartillyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a legendary 1/1 green and white Angelo Dog token")
    void createsAngeloToken() {
        castRinoa();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent angelo = findPermanents(player1, "Angelo").getFirst();
        assertThat(angelo.getEffectivePower()).isEqualTo(1);
        assertThat(angelo.getEffectiveToughness()).isEqualTo(1);
        assertThat(angelo.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(angelo.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(angelo.getCard().getSubtypes()).contains(CardSubtype.DOG);
    }

    @Test
    @DisplayName("Attacking boosts another creature by the number of creatures controlled")
    void attackBoostScalesWithControlledCreatures() {
        addCreatureReady(player1, new RinoaHeartilly());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The attack trigger cannot target Rinoa or an opponent's creature")
    void attackTriggerRequiresAnotherCreatureYouControl() {
        Permanent rinoa = addCreatureReady(player1, new RinoaHeartilly());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, rinoa.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new RinoaHeartilly());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @CardUsed({RinoaHeartilly.class})
    @DisplayName("Angelo can receive the attack boost and opposing creatures are not counted")
    void angeloReceivesBoostCountingOnlyYourCreatures() {
        castRinoa();
        resolveAllTriggers();
        Permanent rinoa = findPermanent(player1, "Rinoa Heartilly");
        Permanent angelo = findPermanent(player1, "Angelo");
        rinoa.setSummoningSick(false);
        addCreatureReady(player2, new RinoaHeartilly());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, angelo.getId());
        harness.passBothPriorities();

        assertThat(angelo.getEffectivePower()).isEqualTo(3);
        assertThat(angelo.getEffectiveToughness()).isEqualTo(3);
        assertThat(rinoa.getPowerModifier()).isZero();
    }

    @Test
    @CardUsed({RinoaHeartilly.class})
    @DisplayName("The attack trigger survives Rinoa leaving and counts creatures at resolution")
    void attackTriggerResolvesAfterRinoaLeaves() {
        castRinoa();
        resolveAllTriggers();
        Permanent rinoa = findPermanent(player1, "Rinoa Heartilly");
        Permanent angelo = findPermanent(player1, "Angelo");
        rinoa.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, angelo.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rinoa);
        harness.setGraveyard(player1, List.of(rinoa.getCard()));
        harness.passBothPriorities();

        assertThat(angelo.getEffectivePower()).isEqualTo(2);
        assertThat(angelo.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @CardUsed({RinoaHeartilly.class})
    @DisplayName("The attack trigger does not boost a target that changes controllers")
    void attackTargetMustStillBeControlledAtResolution() {
        castRinoa();
        resolveAllTriggers();
        Permanent rinoa = findPermanent(player1, "Rinoa Heartilly");
        Permanent angelo = findPermanent(player1, "Angelo");
        rinoa.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, angelo.getId());
        gd.playerBattlefields.get(player1.getId()).remove(angelo);
        gd.playerBattlefields.get(player2.getId()).add(angelo);
        harness.passBothPriorities();

        assertThat(angelo.getEffectivePower()).isEqualTo(1);
        assertThat(angelo.getEffectiveToughness()).isEqualTo(1);
    }

    private void castRinoa() {
        harness.castFromHand(player1, new RinoaHeartilly(), "{3}{G}{W}");
    }
}
