package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreachingHippocamp.class, Island.class})
class BreachingHippocampTest extends BaseCardTest {

    @Test
    @DisplayName("ETB untaps another creature you control")
    void etbUntapsAnotherCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BreachingHippocamp());
        target.tap();

        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects an opponent's creature as target")
    void rejectsOpponentsCreature() {
        harness.addToBattlefield(player2, new BreachingHippocamp());
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Rejects a non-creature as target")
    void rejectsNonCreature() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Can be cast without a target when no other creatures are controlled")
    void canBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's combat")
    void canBeCastDuringOpponentsCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BreachingHippocamp());
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An already untapped creature is a legal target")
    void canTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BreachingHippocamp());
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Entering without being cast still triggers and cannot target itself")
    void enteringWithoutCastingRequiresAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BreachingHippocamp());
        target.tap();

        Permanent source = harness.enterBattlefieldAndReturn(player1, new BreachingHippocamp());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untap trigger does not untap a target now controlled by an opponent")
    void targetMustStillBeControlledAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BreachingHippocamp());
        target.tap();
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untap trigger resolves even after its source leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BreachingHippocamp());
        target.tap();
        BreachingHippocamp sourceCard = new BreachingHippocamp();
        harness.setHand(player1, List.of(sourceCard));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).getLast();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
    }
}
