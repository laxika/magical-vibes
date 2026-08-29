package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HazeFrogTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB prevents other creatures' combat damage but not Haze Frog's")
    void preventsOtherCreaturesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        mystic.setSummoningSick(false);

        harness.setHand(player1, List.of(new HazeFrog()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent frog = findPermanent(player1, "Haze Frog");
        frog.setSummoningSick(false);
        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
