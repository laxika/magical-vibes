package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalScourge.class, ProdigalPyromancer.class, Shock.class, TurnToFrog.class})
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
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new EternalScourge());

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
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new EternalScourge());

        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, scourge.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eternal Scourge");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Eternal Scourge"));
    }

    @Test
    @DisplayName("Does not exile itself when targeted by its controller's spell")
    void doesNotExileWhenTargetedByOwnSpell() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new EternalScourge());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, scourge.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eternal Scourge");
    }

    @Test
    @DisplayName("Does not trigger after losing all abilities")
    void doesNotTriggerAfterLosingAbilities() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new EternalScourge());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, scourge.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, scourge.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Eternal Scourge");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(scourge.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot cast another player's Eternal Scourge from exile without permission")
    void cannotCastAnotherPlayersScourge() {
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player2, List.of(scourge));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, scourge.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(scourge.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting from exile still requires paying the mana cost")
    void cannotCastFromExileWithoutEnoughMana() {
        EternalScourge scourge = new EternalScourge();
        harness.setExile(player1, List.of(scourge));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, scourge.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(scourge.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not exile itself when targeted by its controller's ability")
    void doesNotExileWhenTargetedByOwnAbility() {
        Permanent scourge = harness.addToBattlefieldAndReturn(player1, new EternalScourge());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer),
                null, scourge.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Eternal Scourge");
        assertThat(gd.exiledCards).isEmpty();
    }
}
