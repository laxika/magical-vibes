package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EternalScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast from exile")
    void castFromExile() {
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, scourge.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eternal Scourge");
    }

    @Test
    @DisplayName("Exiles itself when targeted by an opponent's spell")
    void exilesWhenTargetedByOpponentSpell() {
        harness.addToBattlefield(player1, new EternalScourge());
        Permanent scourge = findPermanent(player1, "Eternal Scourge");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, scourge.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eternal Scourge");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Eternal Scourge"));
    }

    @Test
    @DisplayName("Exiles itself when targeted by an opponent's ability")
    void exilesWhenTargetedByOpponentAbility() {
        harness.addToBattlefield(player1, new EternalScourge());
        Permanent scourge = findPermanent(player1, "Eternal Scourge");

        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyromancer = findPermanent(player2, "Prodigal Pyromancer");
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, scourge.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eternal Scourge");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Eternal Scourge"));
    }

    @Test
    @DisplayName("Does not exile itself when targeted by its controller's spell")
    void doesNotExileWhenTargetedByOwnSpell() {
        harness.addToBattlefield(player1, new EternalScourge());
        Permanent scourge = findPermanent(player1, "Eternal Scourge");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, scourge.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eternal Scourge");
    }
}
