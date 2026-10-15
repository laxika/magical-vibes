package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoretuskFirebeast.class, ChandraNalaar.class})
class GoretuskFirebeastTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to target player")
    void etbDealsDamageToPlayer() {
        castAndResolve(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("ETB damage can target its controller")
    void etbDealsDamageToController() {
        castAndResolve(player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("ETB deals 4 damage to target planeswalker")
    void etbDealsDamageToPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 5);
        castAndResolve(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB cannot target a creature")
    void etbCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoretuskFirebeast());
        prepareCard();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can deal lethal damage to its controller's planeswalker")
    void etbCanKillControllersPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player1, 4);

        castAndResolve(planeswalker.getId());

        harness.assertNotOnBattlefield(player1, "Chandra Nalaar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ChandraNalaar);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB damage still resolves after the source leaves the battlefield")
    void etbResolvesWithoutSource() {
        prepareCard();
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goretusk Firebeast");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, java.util.List.of(source.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A planeswalker leaving before ETB resolution does not redirect damage to its controller")
    void etbDoesNotDamageControllerOfRemovedTarget() {
        Permanent planeswalker = addPlaneswalker(player2, 5);
        prepareCard();
        harness.castCreature(player1, 0, 0, planeswalker.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.setGraveyard(player2, java.util.List.of(planeswalker.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goretusk Firebeast");
    }

    private void prepareCard() {
        harness.setHand(player1, java.util.List.of(new GoretuskFirebeast()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void castAndResolve(java.util.UUID targetId) {
        prepareCard();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
