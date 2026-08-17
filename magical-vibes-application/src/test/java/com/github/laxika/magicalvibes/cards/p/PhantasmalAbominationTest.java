package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class PhantasmalAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent abomination = new Permanent(new PhantasmalAbomination());
        abomination.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(abomination);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, abomination.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
    }

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by its controller's spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent abomination = new Permanent(new PhantasmalAbomination());
        abomination.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(abomination);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, abomination.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
    }

    @Test
    @DisplayName("Phantasmal Abomination is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent abomination = new Permanent(new PhantasmalAbomination());
        abomination.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(abomination);

        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyromancer = findPermanent(player2, "Prodigal Pyromancer");
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, abomination.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Abomination");
        harness.assertInGraveyard(player1, "Phantasmal Abomination");
    }
}
