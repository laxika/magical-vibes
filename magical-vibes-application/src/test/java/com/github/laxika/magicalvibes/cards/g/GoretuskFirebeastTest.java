package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoretuskFirebeast.class, GrizzlyBears.class})
class GoretuskFirebeastTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to target player")
    void etbDealsDamageToPlayer() {
        castAndResolve(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
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
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
