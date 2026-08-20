package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JokulmorderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and sacrifices five lands when its controller accepts")
    void entersTappedAndSacrificesFiveLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        castAndResolveJokulmorder();
        Permanent jokulmorder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Jokulmorder)
                .findFirst()
                .orElseThrow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jokulmorder.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        harness.assertOnBattlefield(player1, "Jokulmorder");
    }

    @Test
    @DisplayName("Is sacrificed when its controller cannot sacrifice five lands")
    void isSacrificedWithoutFiveLands() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        castAndResolveJokulmorder();

        harness.assertNotOnBattlefield(player1, "Jokulmorder");
        harness.assertInGraveyard(player1, "Jokulmorder");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(4);
    }

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();

        advanceToNextTurn(player1);

        assertThat(jokulmorder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May untap when its controller plays an Island")
    void mayUntapWhenControllerPlaysIsland() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jokulmorder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when its controller plays a non-Island land")
    void doesNotTriggerForNonIslandLand() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(jokulmorder.isTapped()).isTrue();
    }

    private void castAndResolveJokulmorder() {
        harness.setHand(player1, List.of(new Jokulmorder()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
