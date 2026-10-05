package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MinamoScrollkeeper;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LearnedLearner.class, MinamoScrollkeeper.class, GrizzlyBears.class, Spellbook.class})
class LearnedLearnerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have the draw ability with a maximum hand size of seven")
    void noDrawAbilityWithMaximumHandSizeSeven() {
        addCreatureReady(player1, new LearnedLearner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Has the draw ability when the controller's maximum hand size is not seven")
    void drawsWithMaximumHandSizeOtherThanSeven() {
        Permanent learner = addCreatureReady(player1, new LearnedLearner());
        harness.addToBattlefield(player1, new MinamoScrollkeeper());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int learnerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(learner);

        harness.activateAbility(player1, learnerIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1)
                .contains(drawnCard);
    }

    @Test
    @DisplayName("Having no maximum hand size does not satisfy having a maximum hand size other than seven")
    void noDrawAbilityWithNoMaximumHandSize() {
        addCreatureReady(player1, new LearnedLearner());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new LearnedLearner()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("An opponent's hand size modifier does not grant the draw ability")
    void opponentHandSizeModifierDoesNotGrantAbility() {
        addCreatureReady(player1, new LearnedLearner());
        harness.addToBattlefield(player2, new MinamoScrollkeeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Loses the draw ability when the hand size modifier leaves")
    void losesAbilityWhenModifierLeaves() {
        addCreatureReady(player1, new LearnedLearner());
        Permanent scrollkeeper = harness.addToBattlefieldAndReturn(player1, new MinamoScrollkeeper());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scrollkeeper));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("An activated draw ability resolves after the hand size modifier leaves")
    void activatedAbilityResolvesAfterModifierLeaves() {
        Permanent learner = addCreatureReady(player1, new LearnedLearner());
        Permanent scrollkeeper = harness.addToBattlefieldAndReturn(player1, new MinamoScrollkeeper());
        LearnedLearner drawnCard = new LearnedLearner();
        harness.setLibrary(player1, List.of(drawnCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(learner.isTapped()).isTrue();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scrollkeeper));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1)
                .contains(drawnCard);
    }

    @Test
    @DisplayName("The granted draw ability cannot be activated while summoning sick")
    void cannotDrawWhileSummoningSick() {
        harness.addToBattlefield(player1, new LearnedLearner());
        harness.addToBattlefield(player1, new MinamoScrollkeeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("The granted draw ability cannot be activated twice without untapping")
    void cannotActivateTwiceWithoutUntapping() {
        addCreatureReady(player1, new LearnedLearner());
        harness.addToBattlefield(player1, new MinamoScrollkeeper());
        harness.setLibrary(player1, List.of(new LearnedLearner()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.passBothPriorities();
    }
}
