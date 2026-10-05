package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.ObNixilissCruelty;
import com.github.laxika.magicalvibes.cards.t.TurretOgre;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KioraBehemothBeckoner.class, CrawWurm.class, Forest.class, HillGiant.class,
        ObNixilissCruelty.class, TurretOgre.class})
class KioraBehemothBeckonerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature with power 4 or greater enters under your control")
    void drawsForOwnCreatureWithPowerAtLeastFour() {
        addReadyKiora(player1, 7);
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        int handSizeAfterCreatureEntry = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCreatureEntry + 1);
    }

    @Test
    @DisplayName("Does not draw for a creature with power less than 4")
    void doesNotDrawForSmallerCreature() {
        addReadyKiora(player1, 7);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when an opponent's large creature enters")
    void doesNotDrawForOpponentsCreature() {
        addReadyKiora(player1, 7);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CrawWurm()));
        harness.addMana(player2, ManaColor.GREEN, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("-1 untaps any target permanent")
    void minusOneUntapsTargetPermanent() {
        Permanent kiora = addReadyKiora(player1, 7);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("-1 cannot target a player")
    void minusOneRejectsPlayerTarget() {
        addReadyKiora(player1, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws for a creature entering with exactly four power")
    void drawsForExactlyFourPower() {
        addReadyKiora(player1, 7);
        harness.setHand(player1, List.of(new TurretOgre()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw trigger resolves even if the entering creature leaves the battlefield")
    void drawsAfterEnteringCreatureLeaves() {
        addReadyKiora(player1, 7);
        harness.setHand(player1, List.of(new TurretOgre()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new ObNixilissCruelty()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent ogre = findPermanent(player1, "Turret Ogre");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.castAndResolveInstant(player2, 0, ogre.getId());
        harness.assertNotOnBattlefield(player1, "Turret Ogre");
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Last loyalty counter can pay for untapping another permanent")
    void lastLoyaltyCounterPaysForUntap() {
        addReadyKiora(player1, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.assertNotOnBattlefield(player1, "Kiora, Behemoth Beckoner");
        harness.assertInGraveyard(player1, "Kiora, Behemoth Beckoner");
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    private Permanent addReadyKiora(Player player, int loyalty) {
        Permanent kiora = harness.addToBattlefieldAndReturn(player, new KioraBehemothBeckoner());
        kiora.setCounterCount(CounterType.LOYALTY, loyalty);
        kiora.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return kiora;
    }
}
