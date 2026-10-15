package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KyoshiBattleFan.class, GrizzlyBears.class})
class KyoshiBattleFanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Kyoshi Battle Fan creates an Ally token, attaches to it, and boosts it")
    void enteringCreatesAndAttachesAlly() {
        harness.setHand(player1, List.of(new KyoshiBattleFan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent fan = findPermanent(player1, "Kyoshi Battle Fan");
        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(fan.getAttachedTo()).isEqualTo(ally.getId());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {2} attaches Kyoshi Battle Fan to a creature you control")
    void equipAttachesToCreature() {
        Permanent fan = addReady(player1, new KyoshiBattleFan());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    void reequippingMovesTheBonusWithoutCreatingAnotherToken() {
        harness.setHand(player1, List.of(new KyoshiBattleFan()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent fan = findPermanent(player1, "Kyoshi Battle Fan");
        Permanent ally = findPermanent(player1, "Ally");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void enterTriggerStillCreatesTokenWhenFanHasLeft() {
        harness.setHand(player1, List.of(new KyoshiBattleFan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent fan = findPermanent(player1, "Kyoshi Battle Fan");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, fan));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kyoshi Battle Fan");
        Permanent ally = findPermanent(player1, "Ally");
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(1);
    }

    @Test
    void enteringAnotherFanDoesNotMoveTheExistingFan() {
        harness.setHand(player1, List.of(new KyoshiBattleFan(), new KyoshiBattleFan()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstFan = findPermanent(player1, "Kyoshi Battle Fan");
        Permanent firstAlly = findPermanent(player1, "Ally");

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent secondFan = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof KyoshiBattleFan && p != firstFan)
                .findFirst().orElseThrow();
        assertThat(firstFan.getAttachedTo()).isEqualTo(firstAlly.getId());
        assertThat(secondFan.getAttachedTo()).isNotNull().isNotEqualTo(firstAlly.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, firstAlly)).isEqualTo(2);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent fan = harness.addToBattlefieldAndReturn(player1, new KyoshiBattleFan());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fan.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent fan = harness.addToBattlefieldAndReturn(player1, new KyoshiBattleFan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fan.getAttachedTo()).isNull();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
