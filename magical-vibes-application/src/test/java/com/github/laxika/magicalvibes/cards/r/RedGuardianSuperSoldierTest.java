package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedGuardianSuperSoldier.class, GrizzlyBears.class})
class RedGuardianSuperSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an opponent's creature that dealt damage this turn")
    void etbDestroysOpponentCreatureThatDealtDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a creature controlled by its controller")
    void etbDoesNotDestroyOwnCreatureEvenIfItDealtDamage() {
        Permanent ownBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(ownBears);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(ownBears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGuardian();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not destroy an opponent's creature that dealt no damage this turn")
    void etbDoesNotDestroyOpponentCreatureThatDealtNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGuardian();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castGuardian() {
        harness.setHand(player1, List.of(new RedGuardianSuperSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0);
    }
}
