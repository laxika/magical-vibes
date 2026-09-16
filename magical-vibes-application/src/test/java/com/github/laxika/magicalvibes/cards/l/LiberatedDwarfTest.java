package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiberatedDwarf.class, GiantWarthog.class, SuntailHawk.class})
class LiberatedDwarfTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndBoostsGreenCreatureWithFirstStrike() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(5);
        assertThat(warthog.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        harness.assertInGraveyard(player1, "Liberated Dwarf");
    }

    @Test
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(5);
        assertThat(warthog.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    void canTargetGreenCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(5);
        assertThat(warthog.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    void requiresRedManaToActivate() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warthog.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Liberated Dwarf");
        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(5);
    }

    @Test
    void cannotTargetNonGreenCreature() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
