package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.SokenzanBruiser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KusariGama.class, IsamaruHoundOfKonda.class, HumbleBudoka.class, SokenzanBruiser.class,
        KumanoMasterYamabushi.class})
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

    @Test
    @DisplayName("The Equipment still triggers when another player controls its equipped creature")
    void triggersWithDifferentEquipmentController() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player2).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        blockAttacker(player2, new SokenzanBruiser(), 0);
        addCreatureReady(player2, new HumbleBudoka());

        resolveCombat();

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getCard().getName()).isEqualTo("Kusari-Gama");
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
        });
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Humble Budoka");
    }

    @Test
    @DisplayName("Splash damage hits only the defending player's other creatures")
    void splashDoesNotDamageNoncreaturesOrAttackingPlayersCreatures() {
        Permanent attacker = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player1).setAttachedTo(attacker.getId());
        Permanent friendlyCreature = addCreatureReady(player1, new HumbleBudoka());
        Permanent defendingEquipment = addKusariGama(player2);
        attacker.setAttacking(true);
        blockAttacker(player2, new SokenzanBruiser(), 0);
        addCreatureReady(player2, new HumbleBudoka());

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Humble Budoka");
        assertThat(friendlyCreature.getMarkedDamage()).isZero();
        assertThat(defendingEquipment.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to a blocking creature triggers the Equipment")
    void noncombatDamageToBlockingCreatureTriggers() {
        Permanent kumano = addCreatureReady(player1, new KumanoMasterYamabushi());
        addKusariGama(player1).setAttachedTo(kumano.getId());
        kumano.setAttacking(true);
        blockAttacker(player2, new SokenzanBruiser(), 0);
        Permanent blocker = findPermanent(player2, "Sokenzan Bruiser");
        Permanent bystander = addCreatureReady(player2, new HumbleBudoka());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(bystander.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated pump activations stack and expire at end of turn")
    void repeatedPumpExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addKusariGama(player1).setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    private Permanent addKusariGama(Player player) {
        return addCreatureReady(player, new KusariGama());
    }

    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent perm = addCreatureReady(blocker, blockerCard);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
    }
}
