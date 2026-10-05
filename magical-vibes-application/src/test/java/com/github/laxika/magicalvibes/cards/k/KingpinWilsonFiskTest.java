package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingpinWilsonFisk.class, GrizzlyBears.class, Forest.class, ClawsOfGix.class})
class KingpinWilsonFiskTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Treasures when you sacrifice a creature")
    void createsTwoTreasuresWhenCreatureIsSacrificed() {
        harness.addToBattlefield(player1, new KingpinWilsonFisk());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrifice(bears);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new KingpinWilsonFisk());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice(firstBears);
        resolveAllTriggers();

        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice(secondBears);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when you sacrifice a noncreature permanent")
    void doesNotTriggerForNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KingpinWilsonFisk());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        sacrifice(forest);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Creates two Treasures when Kingpin is sacrificed")
    void createsTwoTreasuresWhenKingpinIsSacrificed() {
        harness.addToBattlefield(player1, new KingpinWilsonFisk());
        harness.addToBattlefield(player1, new ClawsOfGix());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID kingpinId = harness.getPermanentId(player1, "Kingpin, Wilson Fisk");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handlePermanentChosen(player1, kingpinId);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Kingpin after another creature does not trigger again that turn")
    void selfSacrificeSharesOncePerTurnLimit() {
        Permanent kingpin = harness.addToBattlefieldAndReturn(player1, new KingpinWilsonFisk());
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeWithClaws(bears.getId());
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);

        sacrificeWithClaws(kingpin.getId());

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a noncreature does not consume the once-per-turn trigger")
    void noncreatureSacrificeDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new KingpinWilsonFisk());
        harness.addToBattlefield(player1, new ClawsOfGix());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeWithClaws(forest.getId());
        assertThat(countPermanents(player1, "Treasure")).isZero();
        sacrificeWithClaws(bears.getId());

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature sacrifice does not trigger Kingpin")
    void opponentsSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new KingpinWilsonFisk());
        harness.addToBattlefield(player2, new ClawsOfGix());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handlePermanentChosen(player2, bears.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    private void sacrificeWithClaws(UUID permanentId) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int clawsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Claws of Gix"));
        harness.activateAbility(player1, clawsIndex, 0, null, null);
        harness.handlePermanentChosen(player1, permanentId);
        resolveAllTriggers();
    }

    private void sacrifice(Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), card));
    }
}
