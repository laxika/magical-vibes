package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Wight.class, CruelEdict.class, GrizzlyBears.class})
class WightTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new Wight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(exiled -> exiled.card().getId().equals(blocker.getCard().getId()));
        assertThat(findPermanents(player1, "Zombie").stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .isEmpty();
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
