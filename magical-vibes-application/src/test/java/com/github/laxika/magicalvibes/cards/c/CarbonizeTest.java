package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShamanEnKor;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Carbonize.class, DrudgeSkeletons.class, GrizzlyBears.class, Terror.class, ShamanEnKor.class})
class CarbonizeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void dealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Exiles a creature killed by Carbonize")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles the targeted creature if it dies later that turn")
    void exilesCreatureThatDiesLaterThatTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setToughnessModifier(2);

        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Prevents regeneration even when Carbonize damage is prevented")
    void preventsRegenerationWhenDamageIsPrevented() {
        Permanent skeleton = addCreatureReady(player2, new DrudgeSkeletons());
        skeleton.setRegenerationShield(1);
        skeleton.setDamagePreventionShield(3);

        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, skeleton.getId());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        skeleton.setBlocking(true);
        skeleton.addBlockingTargetId(attacker.getId());
        resolveCombat(player1);

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertNotInGraveyard(player2, "Drudge Skeletons");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("A creature receiving redirected Carbonize damage dies normally")
    void redirectedDamageDoesNotExileRecipient() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new ShamanEnKor());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player2, 0, null, bears.getId());
            harness.passBothPriorities();
        }

        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, shaman.getId());

        harness.assertOnBattlefield(player2, "Shaman en-Kor");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature receiving redirected Carbonize damage can regenerate")
    void redirectedDamageDoesNotPreventRecipientRegeneration() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new ShamanEnKor());
        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeleton.setRegenerationShield(1);
        harness.activateAbility(player2, 0, null, skeleton.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, shaman.getId());

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(skeleton.getMarkedDamage()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("Carbonize's exile replacement expires at the end of the turn")
    void exileReplacementExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setDamagePreventionShield(3);
        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
