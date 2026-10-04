package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.t.TezzeretTheSchemer;
import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImplementOfCombustion.class, AetherChaser.class, TezzeretTheSchemer.class})
class ImplementOfCombustionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Implement of Combustion");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent walker = harness.addToBattlefieldAndReturn(player2, new TezzeretTheSchemer());
        walker.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, walker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AetherChaser());
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws a card when it is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.setLibrary(player1, List.of(new AetherChaser()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the draw resolves before the damage")
    void drawsBeforeDamageResolves() {
        harness.addToBattlefield(player1, new ImplementOfCombustion());
        harness.setLibrary(player1, List.of(new AetherChaser()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Implement of Combustion");
        harness.assertInGraveyard(player1, "Implement of Combustion");
        harness.assertNotInHand(player1, "Aether Chaser");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Aether Chaser");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Destruction also triggers the card draw")
    void drawsWhenDestroyed() {
        Permanent implement = harness.addToBattlefieldAndReturn(player1, new ImplementOfCombustion());
        harness.setLibrary(player1, List.of(new AetherChaser()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, implement));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Implement of Combustion");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Aether Chaser");
    }

    @Test
    @DisplayName("The last controller draws even when the artifact belongs to the opponent")
    void lastControllerDrawsInsteadOfOwner() {
        ImplementOfCombustion implement = new ImplementOfCombustion();
        implement.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, implement);
        harness.setLibrary(player1, List.of(new AetherChaser()));
        harness.setLibrary(player2, List.of(new AetherChaser()));
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();
        int ownerHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Implement of Combustion");
        harness.assertNotInGraveyard(player1, "Implement of Combustion");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(ownerHandSize);
    }
}
