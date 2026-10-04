package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KeenEyedArchers;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatbowDoyen.class, KeenEyedArchers.class, GrizzlyBears.class, SerraAngel.class,
        FemerefArchers.class, SkyhunterSkirmisher.class, Terror.class})
class GreatbowDoyenTest extends BaseCardTest {

    // ===== Static: other Archer creatures you control get +1/+1 =====

    @Test
    @DisplayName("Other Archer creatures you control get +1/+1")
    void boostsOtherArchers() {
        addCreatureReady(player1, new GreatbowDoyen());
        addCreatureReady(player1, new KeenEyedArchers());

        Permanent archer = findPermanent(player1, "Keen-Eyed Archers");
        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Greatbow Doyen does not boost itself")
    void doesNotBoostItself() {
        addCreatureReady(player1, new GreatbowDoyen());

        Permanent doyen = findPermanent(player1, "Greatbow Doyen");
        assertThat(gqs.getEffectivePower(gd, doyen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doyen)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost an opponent's Archers")
    void doesNotBoostOpponentArchers() {
        addCreatureReady(player1, new GreatbowDoyen());
        addCreatureReady(player2, new KeenEyedArchers());

        Permanent archer = findPermanent(player2, "Keen-Eyed Archers");
        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(2);
    }

    // ===== Trigger: an Archer you control deals damage to a creature =====

    @Test
    @DisplayName("Greatbow Doyen reflects its own combat damage to the blocker's controller")
    void reflectsOwnCombatDamage() {
        Permanent doyen = addCreatureReady(player1, new GreatbowDoyen());
        doyen.setAttacking(true);
        harness.setLife(player2, 20);

        blockAttacker(player2, new GrizzlyBears(), 0); // Grizzly Bears blocks Doyen (attacker index 0)

        resolveCombat();

        harness.passBothPriorities(); // resolve reflection trigger

        // Doyen (2 power) reflects 2 damage to player2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Reflects combat damage from another Archer you control, using its boosted power")
    void reflectsOtherArcherCombatDamage() {
        addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new KeenEyedArchers());
        archer.setAttacking(true);
        harness.setLife(player2, 20);

        // A 4/4 blocker soaks the boosted 3/3 Archer's full power (and kills it), proving the
        // reflection still fires when the source Archer dies in combat.
        blockAttacker(player2, new SerraAngel(), 1);

        resolveCombat();

        harness.passBothPriorities(); // resolve reflection trigger

        // Keen-Eyed Archers is boosted to 3/3 by Doyen, so it reflects 3 damage to player2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-Archer creature you control does not trigger the reflection")
    void nonArcherDoesNotReflect() {
        addCreatureReady(player1, new GreatbowDoyen());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        blockAttacker(player2, new GrizzlyBears(), 1); // block the Grizzly Bears (attacker index 1)

        resolveCombat();

        // Grizzly Bears is not an Archer, so player2 takes no reflected damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Reflects non-combat damage an Archer deals to a creature")
    void reflectsNoncombatDamage() {
        addCreatureReady(player1, new GreatbowDoyen());
        addCreatureReady(player1, new FemerefArchers());
        harness.setLife(player2, 20);

        Permanent flyer = addCreatureReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);

        // Femeref Archers (an Archer): {T}: deals 4 damage to the attacking flyer.
        harness.activateAbility(player1, 1, null, flyer.getId());
        harness.passBothPriorities(); // resolve ability — deals 4 to the flyer
        harness.passBothPriorities(); // resolve reflection trigger

        // Femeref Archers reflects the 4 damage it dealt to the flyer to player2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(flyer.getId()));
    }

    @Test
    @DisplayName("Another Archer's damage still triggers when Doyen dies in the same damage step")
    void reflectsOtherArcherDamageWhenDoyenDiesSimultaneously() {
        Permanent doyen = addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new FemerefArchers());
        doyen.setAttacking(true);
        archer.setAttacking(true);
        blockAttacker(player2, new SerraAngel(), 0);
        blockAttacker(player2, new SerraAngel(), 1);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Greatbow Doyen");
        harness.assertInGraveyard(player1, "Femeref Archers");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An Archer's activated damage still triggers Doyen after the Archer leaves the battlefield")
    void reflectsNoncombatDamageFromArcherThatDiedInResponse() {
        addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new FemerefArchers());
        Permanent flyer = addCreatureReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 1, null, flyer.getId());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, archer.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Femeref Archers");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Skyhunter Skirmisher");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Multiple Doyens boost each other and each reflect an Archer's damage")
    void multipleDoyensBoostAndTriggerIndependently() {
        Permanent first = addCreatureReady(player1, new GreatbowDoyen());
        Permanent second = addCreatureReady(player1, new GreatbowDoyen());
        Permanent archer = addCreatureReady(player1, new FemerefArchers());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        archer.setAttacking(true);
        blockAttacker(player2, new SerraAngel(), 2);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Damage to a player does not trigger additional damage")
    void unblockedArcherDoesNotReflectPlayerDamage() {
        Permanent doyen = addCreatureReady(player1, new GreatbowDoyen());
        doyen.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's Archer dealing damage does not trigger Doyen")
    void opponentArcherDoesNotReflectDamage() {
        addCreatureReady(player1, new GreatbowDoyen());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        blockAttacker(player2, new FemerefArchers(), 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    /** Adds {@code blockerCard} to {@code blocker}'s battlefield blocking the attacker at {@code attackerIndex}. */
    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent perm = addCreatureReady(blocker, blockerCard);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
    }
}
