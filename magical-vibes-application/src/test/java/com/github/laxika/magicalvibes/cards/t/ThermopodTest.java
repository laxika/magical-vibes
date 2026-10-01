package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed(Thermopod.class)
class ThermopodTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana gives Thermopod haste until end of turn")
    void snowManaGrantsHasteUntilEndOfTurn() {
        Permanent thermopod = addCreatureReady(player1, new Thermopod());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thermopod, Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thermopod, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Regular mana cannot pay Thermopod's snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new Thermopod());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Sacrificing a creature adds one red mana")
    void sacrificingCreatureAddsRedMana() {
        Permanent thermopod = addCreatureReady(player1, new Thermopod());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Thermopod());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thermopod).doesNotContain(fodder);
        harness.assertInGraveyard(player1, "Thermopod");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Thermopod may sacrifice itself to add red mana")
    void thermopodMaySacrificeItself() {
        harness.addToBattlefieldAndReturn(player1, new Thermopod());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Thermopod");
        assertThat(gd.stack).isEmpty();
    }
}
