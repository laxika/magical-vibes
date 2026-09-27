package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.SokenzanBruiser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KusariGama.class, IsamaruHoundOfKonda.class, HumbleBudoka.class, SokenzanBruiser.class})
class KusariGamaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can pay {2} for +1/+0 until end of turn")
    void grantedPumpAbility() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player1).setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} attaches Kusari-Gama to a creature you control")
    void equipAttachesToCreature() {
        Permanent kusariGama = addKusariGama(player1);
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kusariGama.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Damage to a blocker also hits each other creature the defending player controls")
    void damagesOtherDefendingCreatures() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player1).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        blockAttacker(player2, new IsamaruHoundOfKonda(), 0);
        addCreatureReady(player2, new HumbleBudoka());

        resolveCombat();
        harness.passBothPriorities();

        // The 2/2 bystander took the same 2 damage the blocker was dealt.
        harness.assertInGraveyard(player2, "Humble Budoka");
    }

    @Test
    @DisplayName("The blocking creature that was damaged is not dealt the extra damage again")
    void blockingCreatureExcluded() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player1).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // A 3/3 blocker survives the 2 combat damage; a second hit of 2 would kill it.
        blockAttacker(player2, new SokenzanBruiser(), 0);
        addCreatureReady(player2, new HumbleBudoka());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sokenzan Bruiser");
        harness.assertInGraveyard(player2, "Humble Budoka");
    }

    @Test
    @DisplayName("No trigger when the damaged creature is not blocking")
    void noTriggerWhenDamagedCreatureIsNotBlocking() {
        Permanent blocker = addCreatureReady(player1, new SokenzanBruiser());
        addKusariGama(player1).setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HumbleBudoka());

        resolveCombat(player2);

        // The attacker it damaged was not a blocking creature, so nothing else was hit.
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Kusari-Gama"));
        harness.assertOnBattlefield(player2, "Humble Budoka");
    }

    @Test
    @DisplayName("No trigger while the Equipment is unattached")
    void noTriggerWhenUnattached() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player1);
        attacker.setAttacking(true);

        blockAttacker(player2, new IsamaruHoundOfKonda(), 0);
        addCreatureReady(player2, new HumbleBudoka());

        resolveCombat();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Kusari-Gama"));
        harness.assertOnBattlefield(player2, "Humble Budoka");
    }

    private Permanent addKusariGama(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KusariGama());
        perm.setSummoningSick(false);
        return perm;
    }

    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent perm = harness.addToBattlefieldAndReturn(blocker, blockerCard);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
    }
}
