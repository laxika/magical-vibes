package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CinderSeer;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FendOff.class, CinderSeer.class, RecklessAbandon.class, YavimayaHollow.class})
class FendOffTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from the targeted creature")
    void preventsCombatDamageFromTargetCreature() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2);
        castFendOff(attacker);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage only from the targeted creature")
    void preventsOnlyTargetedCreatureCombatDamage() {
        harness.setLife(player1, 20);
        Permanent targetedAttacker = addAttacker(player2);
        addAttacker(player2);
        castFendOff(targetedAttacker);

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from the targeted creature")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new CinderSeer());
        RecklessAbandon redCard = new RecklessAbandon();
        castFendOff(source);

        harness.setHand(player2, List.of(redCard));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(redCard.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaHollow());
        harness.setHand(player1, List.of(new FendOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new FendOff()));
        harness.setLibrary(player1, List.of(new CinderSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fend Off");
        harness.assertInHand(player1, "Cinder Seer");
    }

    @Test
    @DisplayName("Prevents damage to a blocker but does not protect the targeted attacker")
    void preventsDamageToBlockerButNotToAttacker() {
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addCreatureReady(player1, new CinderSeer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        castFendOff(attacker);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Cinder Seer");
        harness.assertNotOnBattlefield(player2, "Cinder Seer");
        harness.assertInGraveyard(player2, "Cinder Seer");
    }

    @Test
    @DisplayName("Can target a friendly blocker and prevent its damage to the attacker")
    void preventsDamageByFriendlyBlocker() {
        addAttacker(player2);
        Permanent blocker = addCreatureReady(player1, new CinderSeer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        castFendOff(blocker);

        resolveCombat(player2);

        harness.assertOnBattlefield(player2, "Cinder Seer");
        harness.assertNotOnBattlefield(player1, "Cinder Seer");
        harness.assertInGraveyard(player1, "Cinder Seer");
    }

    @Test
    @DisplayName("Cycling discards immediately while the draw waits for resolution")
    void cyclingDiscardsAsCost() {
        harness.setHand(player1, List.of(new FendOff()));
        harness.setLibrary(player1, List.of(new CinderSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Fend Off");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Cinder Seer");
    }

    private void castFendOff(Permanent target) {
        harness.setHand(player1, List.of(new FendOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new CinderSeer());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
