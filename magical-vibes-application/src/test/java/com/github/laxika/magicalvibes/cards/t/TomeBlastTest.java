package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomeBlast.class, GrizzlyBears.class})
class TomeBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target creature")
    void dealsTwoDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void damagesOpponentAndGoesToGraveyard() {
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Tome Blast");
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void flashbackDealsDamageAndExilesCard() {
        TomeBlast spell = new TomeBlast();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Tome Blast");
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.findExiledCard(spell.getId()).ownerId()).isEqualTo(player1.getId());
    }

    @Test
    void flashbackRequiresFiveMana() {
        harness.setGraveyard(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Tome Blast");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackExilesEvenWhenTargetLeavesBattlefield() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        TomeBlast spell = new TomeBlast();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castFlashback(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Tome Blast");
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackCannotBeCastDuringCombat() {
        harness.setGraveyard(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        harness.assertInGraveyard(player1, "Tome Blast");
        assertThat(gd.stack).isEmpty();
    }
}
