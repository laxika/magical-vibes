package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TownGossipmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps self and another creature, then transforms")
    void transformTapsSelfAndAnotherCreature() {
        Permanent gossipmonger = addCreatureReady(player1, new TownGossipmonger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(gossipmonger), null, null);
        harness.passBothPriorities();

        assertThat(gossipmonger.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
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

    private Permanent addTransformedRabble() {
        TownGossipmonger card = new TownGossipmonger();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
