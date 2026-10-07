package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeedTheSwarm;
import com.github.laxika.magicalvibes.cards.h.HarmsWay;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.cards.l.LotusCobra;
import com.github.laxika.magicalvibes.cards.t.TurntimberAscetic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpikefieldHazard.class, SpikefieldCave.class, LotusCobra.class,
        JaceMirrorMage.class, TurntimberAscetic.class, FeedTheSwarm.class, HarmsWay.class})
class SpikefieldHazardTest extends BaseCardTest {

    @Test
    void exilesCreatureThatDiesFromItsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LotusCobra());
        castHazard(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void exilesPlaneswalkerThatReachesZeroLoyaltyFromItsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        target.setCounterCount(CounterType.LOYALTY, 1);
        castHazard(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void dealsDamageToAPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpikefieldHazard()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void caveEntersTappedAndProducesRedMana() {
        harness.setHand(player1, List.of(new SpikefieldHazard()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(SpikefieldCave.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void survivingCreatureIsExiledWhenDestroyedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TurntimberAscetic());
        castHazard(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        destroyWithFeedTheSwarm(target);

        harness.assertNotOnBattlefield(player2, "Turntimber Ascetic");
        harness.assertNotInGraveyard(player2, "Turntimber Ascetic");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void exileReplacementExpiresAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TurntimberAscetic());
        castHazard(target.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(target.getMarkedDamage()).isZero();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        destroyWithFeedTheSwarm(target);

        harness.assertInGraveyard(player2, "Turntimber Ascetic");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void redirectedDamageExilesThePermanentThatActuallyReceivesIt() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new TurntimberAscetic());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new LotusCobra());
        SpikefieldHazard hazard = new SpikefieldHazard();
        harness.setHand(player2, List.of(hazard));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, protectedCreature.getId());

        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, recipient.getId());
        harness.handlePermanentChosen(player1, hazard.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Lotus Cobra");
        harness.assertNotInGraveyard(player2, "Lotus Cobra");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(recipient.getCard().getId()));
    }

    @Test
    void targetThatReceivesNoDamageDiesNormallyLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TurntimberAscetic());
        SpikefieldHazard hazard = new SpikefieldHazard();
        harness.setHand(player1, List.of(hazard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new HarmsWay()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.handlePermanentChosen(player2, hazard.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        destroyWithFeedTheSwarm(target);

        harness.assertInGraveyard(player2, "Turntimber Ascetic");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void destroyWithFeedTheSwarm(Permanent target) {
        harness.setHand(player1, List.of(new FeedTheSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void castHazard(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SpikefieldHazard()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
