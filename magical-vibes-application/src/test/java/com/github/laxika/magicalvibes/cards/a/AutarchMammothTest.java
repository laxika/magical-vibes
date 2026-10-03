package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AutarchMammoth.class, HillGiant.class, GrizzlyBears.class})
class AutarchMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Autarch Mammoth creates an Elephant token")
    void entersCreatesElephant() {
        harness.castFromHand(player1, new AutarchMammoth(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().isToken()).isTrue();
        assertThat(elephant.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).containsExactly(CardSubtype.ELEPHANT);
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
        assertThat(elephant.isTapped()).isFalse();
        assertThat(elephant.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Attacking while saddled creates an Elephant token")
    void attacksWhileSaddledCreatesElephant() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        mammoth.setSaddled(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not create an Elephant token")
    void attacksWhileNotSaddledDoesNotCreateElephant() {
        addCreatureReady(player1, new AutarchMammoth());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elephant")).isEmpty();
    }

    @Test
    @DisplayName("Saddle 5 taps other creatures and saddles Autarch Mammoth")
    void saddleTapsOtherCreatures() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mammoth.isSaddled()).isTrue();
        assertThat(hillGiant.isTapped()).isTrue();
        assertThat(grizzlyBears.isTapped()).isTrue();
    }

    @Test
    void saddleCannotUseTheMountItself() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mammoth.isTapped()).isFalse();
        assertThat(mammoth.isSaddled()).isFalse();
    }

    @Test
    void saddleRejectsInsufficientOtherPower() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.isTapped()).isFalse();
        assertThat(mammoth.isSaddled()).isFalse();
    }

    @Test
    void summoningSickCreatureCanSaddleAndMountCanAttack() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new AutarchMammoth());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(mammoth.isTapped()).isFalse();
        assertThat(mammoth.isSaddled()).isTrue();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
        assertThat(findPermanent(player1, "Elephant").isAttacking()).isFalse();
    }

    @Test
    void saddleCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new AutarchMammoth());
        Permanent helper = addCreatureReady(player1, new AutarchMammoth());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
    }

    @Test
    void saddledAttackTriggerResolvesAfterMountLeavesBattlefield() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        mammoth.setSaddled(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(mammoth);
        gd.playerGraveyards.get(player1.getId()).add(mammoth.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
    }
}
