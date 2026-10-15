package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KjeldoranEscort;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldeviSteamBeast.class, KjeldoranEscort.class, WitchbaneOrb.class})
class SoldeviSteamBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps the Beast and gives the opponent 2 life")
    void attackingGivesOpponentTwoLife() {
        addCreatureReady(player1, new SoldeviSteamBeast());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        // 20 + 2 from the tap trigger - 4 unblocked combat damage.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another creature of the controller becoming tapped does not trigger the Beast")
    void otherCreatureTappingDoesNotTrigger() {
        addCreatureReady(player1, new SoldeviSteamBeast());
        addCreatureReady(player1, new KjeldoranEscort());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        // 20 - 2 combat damage only; no life gained because the Beast stayed untapped.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("{2} regenerates the Beast from lethal combat damage")
    void regeneratesFromLethalDamage() {
        Permanent beast = addCreatureReady(player1, new SoldeviSteamBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(beast.getRegenerationShield()).isEqualTo(1);
        harness.setLife(player2, 20);

        beast.setBlocking(true);
        beast.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KjeldoranEscort());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Soldevi Steam Beast");
        Permanent regenerated = findPermanent(player1, "Soldevi Steam Beast");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Without a shield the Beast dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent beast = addCreatureReady(player1, new SoldeviSteamBeast());

        beast.setBlocking(true);
        beast.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new KjeldoranEscort());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Soldevi Steam Beast");
    }

    @Test
    @DisplayName("An opponent with hexproof cannot receive the targeted tap trigger")
    void hexproofOpponentCannotBeTargeted() {
        addCreatureReady(player1, new SoldeviSteamBeast());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Creating a regeneration shield does not tap the Beast or give life")
    void creatingShieldDoesNotTriggerLifeGain() {
        Permanent beast = addCreatureReady(player1, new SoldeviSteamBeast());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(beast.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Regenerating an already tapped Beast does not trigger another life gain")
    void regeneratingTappedBeastDoesNotTriggerLifeGain() {
        Permanent beast = addCreatureReady(player1, new SoldeviSteamBeast());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        beast.tap();
        beast.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KjeldoranEscort());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Soldevi Steam Beast");
        assertThat(beast.isTapped()).isTrue();
        assertThat(beast.isAttacking()).isFalse();
        assertThat(beast.getMarkedDamage()).isZero();
        assertThat(beast.getRegenerationShield()).isZero();
        harness.assertLife(player2, 20);
    }
}
