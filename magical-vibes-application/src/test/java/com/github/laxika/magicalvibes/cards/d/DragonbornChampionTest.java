package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonbornChampion.class, Forest.class, LavaAxe.class, ShivanDragon.class})
class DragonbornChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a controlled source deals exactly 5 damage")
    void drawsAtThreshold() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent champion = addCreatureReady(player1, new DragonbornChampion());
        champion.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a controlled source deals less than 5 damage")
    void doesNotDrawBelowThreshold() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        DragonbornChampion card = new DragonbornChampion();
        card.setPower(4);
        Permanent champion = addCreatureReady(player1, card);
        champion.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage below five does not put an ability on the stack")
    void belowThresholdDoesNotTrigger() {
        DragonbornChampion card = new DragonbornChampion();
        card.setPower(4);
        addCreatureReady(player1, card).setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws when another controlled creature deals more than five damage")
    void drawsForAnotherCreatureAboveThreshold() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new DragonbornChampion());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        dragon.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws for five noncombat damage dealt by a controlled spell")
    void drawsForControlledSpellDamage() {
        addCreatureReady(player1, new DragonbornChampion());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when a controlled spell deals five damage to its own controller")
    void drawsForDamageToOwnController() {
        addCreatureReady(player1, new DragonbornChampion());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for damage dealt by an opponent's spell")
    void doesNotDrawForOpponentSource() {
        addCreatureReady(player1, new DragonbornChampion());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
