package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessAbandon.class, GoliathBeetle.class, PlatedSpider.class, ThranDynamo.class})
class RecklessAbandonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 4 damage to a target player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        harness.assertInGraveyard(player1, "Goliath Beetle");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals 4 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());

        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        harness.assertNotOnBattlefield(player2, "Plated Spider");
        harness.assertInGraveyard(player2, "Plated Spider");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent as an additional cost")
    void cannotSacrificeNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranDynamo());

        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Sacrifice target must be a creature");
        harness.assertOnBattlefield(player1, "Thran Dynamo");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
