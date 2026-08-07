package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RadiatingLightningTest extends BaseCardTest {

    // ===== Damage to player =====

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Deals 1 damage to each creature target player controls")
    void deals1DamageToEachCreature() {
        harness.setLife(player2, 20);
        // Grizzly Bears is 2/2 — survives 1 damage
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Both Grizzly Bears should survive with 1 damage
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 1);
        // Radiating Lightning has no "attacks this turn if able" rider — Aggravate's flag must not
        // ride along on every card sharing the effect.
        assertThat(battlefield).noneMatch(Permanent::isMustAttackThisTurn);
    }

    @Test
    @DisplayName("Kills 1-toughness creatures")
    void kills1ToughnessCreatures() {
        harness.setLife(player2, 20);
        // Add a 1/1 creature to player2
        Permanent oneOne = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(oneOne).setToughness(1);
        TestCards.mutableCard(oneOne).setPower(1);
        gd.playerBattlefields.get(player2.getId()).add(oneOne);

        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaves the target player's planeswalker alone — only creatures are damaged")
    void leavesTheTargetPlayersPlaneswalkerAlone() {
        harness.setLife(player2, 20);
        Permanent ajani = new Permanent(new AjaniOutlandChaperone());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ajani);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // The Bear takes its 1 damage; the planeswalker keeps every loyalty counter.
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ajani, Outland Chaperone");
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    // ===== Does not affect caster's creatures =====

    @Test
    @DisplayName("Does not damage caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        List<Permanent> casterBattlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(casterBattlefield).hasSize(1);
        assertThat(casterBattlefield.getFirst().getMarkedDamage()).isZero();
    }

    // ===== Combined effect =====

    @Test
    @DisplayName("Deals 3 to player and 1 to each creature simultaneously")
    void dealsBothDamages() {
        harness.setLife(player2, 20);
        // Hill Giant is 3/3 — survives 1 damage
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player takes 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Both creatures survive with 1 damage each
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 1);
    }

    // ===== No creatures =====

    @Test
    @DisplayName("Works when target player controls no creatures")
    void worksWithNoCreatures() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RadiatingLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
