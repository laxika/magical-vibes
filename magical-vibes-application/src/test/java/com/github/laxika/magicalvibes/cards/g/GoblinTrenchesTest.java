package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTrenches.class, Mountain.class})
class GoblinTrenchesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying two mana and sacrificing a land creates two Goblin Soldier tokens")
    void createsTwoGoblinSoldierTokens() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinTrenches());
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Goblin Soldier")).isEqualTo(2);
        assertThat(findPermanents(player1, "Goblin Soldier")).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.SOLDIER);
        });
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new GoblinTrenches());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Goblin Soldier")).isEmpty();
    }

    @Test
    @DisplayName("A nonland permanent cannot be sacrificed when a land is available")
    void sacrificesOnlyLands() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinTrenches());
        harness.addToBattlefield(player1, new GoblinTrenches());
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Goblin Trenches")).isEqualTo(2);
        assertThat(countPermanents(player1, "Goblin Soldier")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The ability cannot be activated without two mana")
    void requiresTwoMana() {
        harness.addToBattlefield(player1, new GoblinTrenches());
        harness.addToBattlefield(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Goblin Trenches")).isEqualTo(1);
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin Soldier")).isEmpty();
    }
}
