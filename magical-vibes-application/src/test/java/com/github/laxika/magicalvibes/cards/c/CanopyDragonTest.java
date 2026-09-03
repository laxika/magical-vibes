package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CanopyDragon.class)
class CanopyDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Canopy Dragon has trample and no flying by default")
    void tramplerByDefault() {
        Permanent dragon = addCreatureReady(player1, new CanopyDragon());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{1}{G}: Canopy Dragon gains flying and loses trample")
    void activationSwapsTrampleForFlying() {
        Permanent dragon = addCreatureReady(player1, new CanopyDragon());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability changes only the Canopy Dragon that activated it")
    void activationAffectsOnlyItsSource() {
        Permanent dragon = addCreatureReady(player1, new CanopyDragon());
        Permanent otherDragon = addCreatureReady(player1, new CanopyDragon());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherDragon, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherDragon, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without {1}{G}")
    void activationRequiresTwoManaIncludingGreen() {
        addCreatureReady(player1, new CanopyDragon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The non-tapping ability can be activated while the Dragon has summoning sickness")
    void activationDoesNotRequireHaste() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new CanopyDragon());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThat(dragon.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The swap wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new CanopyDragon());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.TRAMPLE)).isTrue();
    }
}
