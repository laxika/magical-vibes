package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeastingTrollKing;
import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavvyHunter.class, Memnite.class, FeastingTrollKing.class, Gingerbrute.class, EchoCirclet.class})
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

    @Test
    @DisplayName("The Food created by attacking can be sacrificed for three life immediately")
    void generatedFoodGainsLife() {
        addReadyHunter(player1);
        harness.setLife(player1, 10);
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, food), null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hunter can sacrifice tapped nontoken Foods")
    void sacrificesNontokenFoodsWithoutTappingHunter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new SavvyHunter());
        hunter.setTapped(true);
        hunter.setSummoningSick(true);
        Permanent firstFood = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Permanent secondFood = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        firstFood.setTapped(true);
        secondFood.setTapped(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SavvyHunter()));

        harness.activateAbility(player1, indexOf(player1, hunter), null, null);
        harness.handlePermanentChosen(player1, firstFood.getId());
        harness.handlePermanentChosen(player1, secondFood.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gingerbrute")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Savvy Hunter");
        assertThat(hunter.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Savvy Hunter");
    }

    @Test
    @DisplayName("One Food and a non-Food permanent cannot pay the draw cost")
    void cannotActivateWithOnlyOneFood() {
        Permanent hunter = addReadyHunter(player1);
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.addToBattlefield(player1, new FeastingTrollKing());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hunter), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gingerbrute");
        harness.assertOnBattlefield(player1, "Feasting Troll King");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Food cannot pay the draw cost")
    void cannotSacrificeOpponentsFood() {
        Permanent hunter = addReadyHunter(player1);
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.addToBattlefield(player2, new Gingerbrute());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hunter), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gingerbrute");
        harness.assertOnBattlefield(player2, "Gingerbrute");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocking two attackers creates only one Food")
    void blockingMultipleCreaturesCreatesOnlyOneFood() {
        addCreatureReady(player1, new Memnite()).setAttacking(true);
        addCreatureReady(player1, new Memnite()).setAttacking(true);
        Permanent hunter = addReadyHunter(player2);
        Permanent circlet = harness.addToBattlefieldAndReturn(player2, new EchoCirclet());
        circlet.setAttachedTo(hunter.getId());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Food")).isOne();
    }

    private Permanent addReadyHunter(Player player) {
        return addCreatureReady(player, new SavvyHunter());
    }

    private void castFeastingTrollKing() {
        harness.setHand(player1, List.of(new FeastingTrollKing()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
