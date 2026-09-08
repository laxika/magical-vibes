package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ComeBackWrong.class, GrizzlyBears.class, FountainOfYouth.class})
class ComeBackWrongTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's creature and returns it under your control")
    void destroysAndReturnsCreatureUnderYourControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castComeBackWrong(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(returned.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return an indestructible creature")
    void doesNotReturnIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castComeBackWrong(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Can target only a creature")
    void canTargetOnlyCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ComeBackWrong()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castComeBackWrong(Permanent target) {
        harness.setHand(player1, List.of(new ComeBackWrong()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
