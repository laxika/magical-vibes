package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JackalPup;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiberatedDwarf.class, GrizzlyBears.class, JackalPup.class})
class LiberatedDwarfTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndBoostsGreenCreatureWithFirstStrike() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        harness.assertInGraveyard(player1, "Liberated Dwarf");
    }

    @Test
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    void cannotTargetNonGreenCreature() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent pup = harness.addToBattlefieldAndReturn(player2, new JackalPup());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pup.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
