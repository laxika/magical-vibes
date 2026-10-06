package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedHerring.class})
class RedHerringTest extends BaseCardTest {

    @Test
    @DisplayName("Red Herring must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new RedHerring());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Paying two mana sacrifices Red Herring and draws a card")
    void sacrificesAndDraws() {
        Permanent redHerring = addCreatureReady(player1, new RedHerring());
        RedHerring drawnCard = new RedHerring();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(redHerring);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(redHerring.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Haste requires Red Herring to attack even on the turn it enters")
    void mustAttackWhileSummoningSick() {
        Permanent redHerring = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        redHerring.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Red Herring can attack and deal damage on the turn it enters")
    void hasteAllowsCombatDamage() {
        Permanent redHerring = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        redHerring.setSummoningSick(true);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped Red Herring is not required to attack")
    void tappedCreatureNeedNotAttack() {
        Permanent redHerring = addCreatureReady(player1, new RedHerring());
        redHerring.setTapped(true);

        declareAttackers(List.of());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Red Herring");
    }

    @Test
    @DisplayName("A tapped Red Herring can be sacrificed during the opponent's turn")
    void canActivateWhileTappedOnOpponentsTurn() {
        Permanent redHerring = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        redHerring.setSummoningSick(true);
        redHerring.setTapped(true);
        RedHerring drawnCard = new RedHerring();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(redHerring.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(drawnCard);
    }

    @Test
    @DisplayName("One mana cannot pay for the sacrifice ability")
    void insufficientManaDoesNotSacrificeOrDraw() {
        Permanent redHerring = addCreatureReady(player1, new RedHerring());
        RedHerring drawnCard = new RedHerring();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(redHerring);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(redHerring.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.stack).isEmpty();
    }
}
