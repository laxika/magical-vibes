package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TownGossipmonger.class})
class TownGossipmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps self and another creature, then transforms")
    void transformTapsSelfAndAnotherCreature() {
        Permanent gossipmonger = addCreatureReady(player1, new TownGossipmonger());
        Permanent helper = addCreatureReady(player1, new TownGossipmonger());

        harness.activateAbility(player1, indexOf(gossipmonger), null, null);
        harness.passBothPriorities();

        assertThat(gossipmonger.isTapped()).isTrue();
        assertThat(helper.isTapped()).isTrue();
        assertThat(gossipmonger.isTransformed()).isTrue();
        assertThat(gossipmonger.getCard().getName()).isEqualTo("Incited Rabble");
    }

    @Test
    @DisplayName("Cannot transform without another untapped creature")
    void cannotTransformAlone() {
        Permanent gossipmonger = addCreatureReady(player1, new TownGossipmonger());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(gossipmonger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature");
    }

    @Test
    @DisplayName("Transformed Incited Rabble must attack if able")
    void transformedMustAttack() {
        Permanent rabble = addTransformedRabble();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(rabble.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("{2} gives Incited Rabble +1/+0 until end of turn")
    void boostUntilEndOfTurn() {
        Permanent rabble = addTransformedRabble();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(rabble), null, null);
        harness.passBothPriorities();

        assertThat(rabble.getEffectivePower()).isEqualTo(3);
        assertThat(rabble.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rabble.getEffectivePower()).isEqualTo(2);
        assertThat(rabble.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick helper can pay the additional tap cost before resolution")
    void summoningSickHelperPaysCostImmediately() {
        Permanent gossipmonger = addCreatureReady(player1, new TownGossipmonger());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new TownGossipmonger());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(gossipmonger), null, null);

        assertThat(gossipmonger.isTapped()).isTrue();
        assertThat(helper.isTapped()).isTrue();
        assertThat(gossipmonger.isTransformed()).isFalse();

        harness.passBothPriorities();

        assertThat(gossipmonger.isTransformed()).isTrue();
        assertThat(gossipmonger.isSummoningSick()).isFalse();
        assertThat(helper.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating Town Gossipmonger's tap ability")
    void summoningSickSourceCannotActivate() {
        Permanent gossipmonger = harness.addToBattlefieldAndReturn(player1, new TownGossipmonger());
        gossipmonger.setSummoningSick(true);
        Permanent helper = addCreatureReady(player1, new TownGossipmonger());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(gossipmonger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gossipmonger.isTapped()).isFalse();
        assertThat(helper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay the additional tap cost")
    void cannotUseTappedOrOpposingHelper() {
        Permanent gossipmonger = addCreatureReady(player1, new TownGossipmonger());
        Permanent helper = addCreatureReady(player1, new TownGossipmonger());
        helper.tap();
        Permanent opponent = addCreatureReady(player2, new TownGossipmonger());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(gossipmonger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature");

        assertThat(gossipmonger.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(gossipmonger.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("When several helpers are available, only the chosen creature is tapped")
    void choosesOneHelper() {
        Permanent gossipmonger = addCreatureReady(player1, new TownGossipmonger());
        Permanent first = addCreatureReady(player1, new TownGossipmonger());
        Permanent chosen = addCreatureReady(player1, new TownGossipmonger());

        harness.activateAbility(player1, indexOf(gossipmonger), null, null);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gossipmonger.isTapped()).isTrue();
        assertThat(first.isTapped()).isFalse();
        assertThat(chosen.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gossipmonger.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("A tapped Incited Rabble is not required to attack")
    void tappedRabbleNeedNotAttack() {
        Permanent rabble = addTransformedRabble();
        rabble.tap();

        declareAttackers(List.of());

        assertThat(rabble.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Incited Rabble is not required to attack")
    void summoningSickRabbleNeedNotAttack() {
        Permanent rabble = addTransformedRabble();
        rabble.setSummoningSick(true);

        declareAttackers(List.of());

        assertThat(rabble.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Incited Rabble can repeatedly pump while tapped and summoning sick")
    void pumpStacksWithoutTapOrSummoningSicknessRestriction() {
        Permanent rabble = addTransformedRabble();
        rabble.tap();
        rabble.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(rabble), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(rabble), null, null);
        harness.passBothPriorities();

        assertThat(rabble.getEffectivePower()).isEqualTo(4);
        assertThat(rabble.getEffectiveToughness()).isEqualTo(3);
        assertThat(rabble.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rabble.getEffectivePower()).isEqualTo(2);
        assertThat(rabble.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Incited Rabble cannot pump with less than two mana")
    void pumpRequiresTwoMana() {
        Permanent rabble = addTransformedRabble();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(rabble), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rabble.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTransformedRabble() {
        TownGossipmonger card = new TownGossipmonger();
        Permanent perm = addCreatureReady(player1, card);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
