package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PurphorossIntervention.class, CrawWurm.class, Millstone.class})
class PurphorossInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one X/1 Elemental token with trample and haste")
    void createsElementalToken() {
        harness.setHand(player1, List.of(new PurphorossIntervention()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castModalInstantForX(player1, 0, 0, 3, null);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getCard().getPower()).isEqualTo(3);
        assertThat(elemental.getCard().getToughness()).isEqualTo(1);
        assertThat(elemental.getCard().getKeywords())
                .contains(Keyword.TRAMPLE, Keyword.HASTE);
    }

    @Test
    @DisplayName("Sacrifices the Elemental token at the beginning of the next end step")
    void sacrificesElementalTokenAtNextEndStep() {
        harness.setHand(player1, List.of(new PurphorossIntervention()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantForX(player1, 0, 0, 2, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elemental");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    @Test
    @DisplayName("Deals twice X damage to a target creature")
    void dealsTwiceXDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player1, List.of(new PurphorossIntervention()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantForX(player1, 0, 1, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("The damage mode cannot target a noncreature, non-planeswalker permanent")
    void damageModeRequiresCreatureOrPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new PurphorossIntervention()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstantForX(player1, 0, 1, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }
}
