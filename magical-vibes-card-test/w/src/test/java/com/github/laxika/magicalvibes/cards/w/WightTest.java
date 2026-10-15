package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FeignDeath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.cards.s.SilverRaven;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Wight.class, CruelEdict.class, GrizzlyBears.class,
        FeignDeath.class, PowerWordKill.class, SilverRaven.class})
class WightTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new Wight(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wight").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles a creature it kills and creates a tapped Zombie")
    void killsCreatureAndCreatesTappedZombie() {
        Permanent wight = addReadyCreature(player1, new Wight());
        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(1);
        Permanent blocker = addReadyCreature(player2, blockerCard);
        wight.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card().getId().equals(blocker.getCard().getId()));

        List<Permanent> zombies = findPermanents(player1, "Zombie").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().isTapped()).isTrue();
        assertThat(zombies.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombies.getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers when a creature it damaged dies later in the turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent wight = addReadyCreature(player1, new Wight());
        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(1);
        blockerCard.setToughness(5);
        Permanent blocker = addReadyCreature(player2, blockerCard);
        wight.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatDamage();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card().getId().equals(blocker.getCard().getId()));
        assertThat(findPermanents(player1, "Zombie").stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void doesNotTriggerForUndamagedCreature() {
        addReadyCreature(player1, new Wight());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(exiled -> exiled.card().getId().equals(blocker.getCard().getId()));
        assertThat(findPermanents(player1, "Zombie").stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .isEmpty();
    }

    @Test
    @DisplayName("Both Wights trigger when they kill each other simultaneously")
    void triggersWhenWightDiesSimultaneouslyWithDamagedCreature() {
        Permanent attacker = addReadyCreature(player1, new Wight());
        Permanent blocker = addReadyCreature(player2, new Wight());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatDamage();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wight");
        harness.assertNotOnBattlefield(player2, "Wight");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(attacker.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(blocker.getCard().getId()));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).hasSize(1);
        assertThat(findPermanent(player1, "Zombie").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Zombie").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a Zombie even when the dead creature has already returned")
    void createsTokenWhenDeadCardIsNoLongerInGraveyard() {
        Permanent returned = returnDamagedRavenBeforeWightTrigger();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returned);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(returned.getCard().getId()));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanent(player1, "Zombie").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not exile a new graveyard object after the creature returns and dies again")
    void doesNotExileCardFromSecondGraveyardVisit() {
        Permanent returned = returnDamagedRavenBeforeWightTrigger();
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        harness.assertInGraveyard(player2, "Silver Raven");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Silver Raven");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(returned.getCard().getId()));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    private Permanent returnDamagedRavenBeforeWightTrigger() {
        Permanent wight = addReadyCreature(player1, new Wight());
        Permanent raven = addReadyCreature(player2, new SilverRaven());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new FeignDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, raven.getId());
        wight.setAttacking(true);
        raven.setBlocking(true);
        raven.addBlockingTarget(0);

        resolveCombatDamage();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Silver Raven");
        assertThat(returned.getId()).isNotEqualTo(raven.getId());
        harness.assertNotInGraveyard(player2, "Silver Raven");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        return returned;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void resolveCombatAndTrigger() {
        resolveCombatDamage();
        harness.passBothPriorities();
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
