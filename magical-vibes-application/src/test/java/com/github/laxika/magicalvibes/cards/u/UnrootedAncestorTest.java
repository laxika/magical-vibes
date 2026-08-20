package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnrootedAncestorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants indestructible and taps Unrooted Ancestor")
    void sacrificeAnotherCreatureGrantsIndestructibleAndTapsSource() {
        Permanent ancestor = addCreatureReady(player1, new UnrootedAncestor());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(ancestor.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(ancestor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot sacrifice Unrooted Ancestor itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new UnrootedAncestor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Unrooted Ancestor");
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent ancestor = addCreatureReady(player1, new UnrootedAncestor());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(ancestor.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ancestor.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }
}
