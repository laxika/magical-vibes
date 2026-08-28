package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThassaDeepDwelling.class, FugitiveWizard.class, GrizzlyBears.class})
class ThassaDeepDwellingTest extends BaseCardTest {

    @Test
    @DisplayName("Thassa is not a creature below five blue devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent thassa = addThassa();

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa becomes a creature at five blue devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent thassa = addThassa();
        addBluePermanents(4);

        assertThat(gqs.isCreature(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("The end-step ability immediately flickers another creature you control")
    void flickersAnotherCreatureAtEndStep() {
        addThassa();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("The end-step ability can resolve without choosing a creature")
    void endStepAbilityCanChooseNoCreature() {
        addThassa();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("The activated ability taps another target creature")
    void tapsAnotherCreature() {
        addThassa();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target Thassa itself")
    void cannotTargetItself() {
        Permanent thassa = addThassa();
        addBluePermanents(4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thassa.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    private Permanent addThassa() {
        return harness.addToBattlefieldAndReturn(player1, new ThassaDeepDwelling());
    }

    private void addBluePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new FugitiveWizard());
        }
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
