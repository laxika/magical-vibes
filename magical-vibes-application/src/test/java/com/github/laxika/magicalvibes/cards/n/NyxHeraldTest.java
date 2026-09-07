package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxHerald.class, GrizzlyBears.class, HolyStrength.class, NyxbornCourser.class})
class NyxHeraldTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Boosts and grants trample to an enchanted creature")
    void boostsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boosts and grants trample to an enchantment creature")
    void boostsEnchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an unenchanted non-enchantment creature")
    void rejectsIneligibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new NyxHerald());
        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temporary boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxHerald());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }
}
