package com.github.laxika.magicalvibes.cards.j;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.b.BasilicaSkullbomb;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Obliterate;
import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JawsRelentlessPredator.class, BasilicaSkullbomb.class, QasaliPridemage.class,
        LeoninScimitar.class, Obliterate.class})
class JawsRelentlessPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Blood tokens equal to combat damage dealt to a player")
    void createsBloodTokensEqualToCombatDamage() {
        Permanent jaws = addCreatureReady(player1, new JawsRelentlessPredator());
        jaws.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 1 damage to each opponent when a noncreature artifact is sacrificed")
    void damagesOpponentsWhenArtifactIsSacrificed() {
        addCreatureReady(player1, new JawsRelentlessPredator());
        harness.addToBattlefield(player1, new BasilicaSkullbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to each opponent when a noncreature artifact is destroyed")
    void damagesOpponentsWhenArtifactIsDestroyed() {
        addCreatureReady(player1, new JawsRelentlessPredator());
        addCreatureReady(player1, new QasaliPridemage());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, scimitar.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An opponent sacrificing an artifact damages that opponent, not Jaws's controller")
    void opponentSacrificeTriggersDamage() {
        addCreatureReady(player1, new JawsRelentlessPredator());
        harness.addToBattlefield(player2, new BasilicaSkullbomb());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Creates tokens using the combat damage amount even if Jaws's power changes before resolution")
    void snapshotsCombatDamageAmount() {
        Permanent jaws = addCreatureReady(player1, new JawsRelentlessPredator());
        jaws.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        jaws.setAttacking(true);

        resolveCombat();
        jaws.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(7);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Destroying a generated Blood token triggers damage, but sacrificing a creature does not")
    void destroyedBloodTokenTriggersDamage() {
        Permanent jaws = addCreatureReady(player1, new JawsRelentlessPredator());
        addCreatureReady(player1, new QasaliPridemage());
        jaws.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, blood.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Generated Blood can be sacrificed to draw, triggering Jaws as its cost is paid")
    void bloodActivationTriggersDamageAndDraws() {
        Permanent jaws = addCreatureReady(player1, new JawsRelentlessPredator());
        jaws.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.setLibrary(player1, List.of(new BasilicaSkullbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Blood")).isEqualTo(4);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        harness.assertInHand(player1, "Basilica Skullbomb");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Triggers for every noncreature artifact destroyed simultaneously with Jaws")
    void triggersWhenDestroyedWithArtifacts() {
        addCreatureReady(player1, new JawsRelentlessPredator());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BasilicaSkullbomb());

        harness.castFromHand(player1, new Obliterate(), "{6}{R}{R}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Jaws, Relentless Predator");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Basilica Skullbomb");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
