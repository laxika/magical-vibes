package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoldeviGolem;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrsOfKorlis.class, Shock.class, SoldeviGolem.class, Triskelion.class})
class MartyrsOfKorlisTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Martyrs of Korlis redirects artifact combat damage to itself")
    void untappedRedirectsArtifactCombatDamage() {
        Permanent martyrs = addCreatureReady(player2, new MartyrsOfKorlis());
        Permanent golem = addCreatureReady(player1, new SoldeviGolem());
        golem.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.damageDealtToPermanentsThisTurn.getOrDefault(martyrs.getId(), 0)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Tapped Martyrs of Korlis does not redirect artifact combat damage")
    void tappedDoesNotRedirectArtifactCombatDamage() {
        Permanent martyrs = addCreatureReady(player2, new MartyrsOfKorlis());
        martyrs.tap();
        Permanent golem = addCreatureReady(player1, new SoldeviGolem());
        golem.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
        assertThat(martyrs.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage from nonartifact sources is dealt to the controller")
    void doesNotRedirectNonartifactDamage() {
        Permanent martyrs = addCreatureReady(player2, new MartyrsOfKorlis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(martyrs.getMarkedDamage()).isZero();
    }

    @Test
    void redirectsAllSimultaneousArtifactDamageEvenWhenLethal() {
        addCreatureReady(player2, new MartyrsOfKorlis());
        for (int i = 0; i < 3; i++) {
            Permanent golem = addCreatureReady(player1, new SoldeviGolem());
            golem.setAttacking(true);
        }

        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Martyrs of Korlis");
    }

    @Test
    void redirectsNoncombatDamageFromArtifactAbility() {
        Permanent martyrs = addCreatureReady(player2, new MartyrsOfKorlis());
        harness.castFromHand(player1, new Triskelion(), "{6}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(martyrs.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotRedirectArtifactDamageToAnotherCreature() {
        Permanent martyrs = addCreatureReady(player2, new MartyrsOfKorlis());
        Permanent other = addCreatureReady(player2, new MartyrsOfKorlis());
        harness.castFromHand(player1, new Triskelion(), "{6}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, other.getId());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(1);
        assertThat(martyrs.getMarkedDamage()).isZero();
    }
}
