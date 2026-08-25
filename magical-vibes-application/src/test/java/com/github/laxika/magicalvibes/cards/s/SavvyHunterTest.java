package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeastingTrollKing;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavvyHunter.class, Memnite.class, FeastingTrollKing.class})
class SavvyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it attacks")
    void attackingCreatesFood() {
        addReadyHunter(player1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Creates a Food token when it blocks")
    void blockingCreatesFood() {
        Permanent attacker = addCreatureReady(player1, new Memnite());
        attacker.setAttacking(true);
        addReadyHunter(player2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Food")).isOne();
    }

    @Test
    @DisplayName("Sacrificing two Foods draws a card")
    void sacrificesTwoFoodsToDraw() {
        castFeastingTrollKing();
        Permanent hunter = addReadyHunter(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new SavvyHunter()));

        harness.activateAbility(player1, indexOf(player1, hunter), null, null);
        List<Permanent> foods = findPermanents(player1, "Food");
        harness.handlePermanentChosen(player1, foods.get(0).getId());
        harness.handlePermanentChosen(player1, foods.get(1).getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyHunter(Player player) {
        return addCreatureReady(player, new SavvyHunter());
    }

    private void castFeastingTrollKing() {
        harness.setHand(player1, List.of(new FeastingTrollKing()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
