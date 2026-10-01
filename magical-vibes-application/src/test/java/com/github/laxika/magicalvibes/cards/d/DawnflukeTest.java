package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dawnfluke.class, AvianChangeling.class})
class DawnflukeTest extends BaseCardTest {

    private Permanent addTargetCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new AvianChangeling());
    }

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB prevents the next 3 damage to the target and Dawnfluke stays on the battlefield")
    void hardcastAppliesShieldAndStays() {
        Permanent target = addTargetCreature(player1);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(target.getDamagePreventionShield()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Dawnfluke");
        harness.assertNotInGraveyard(player1, "Dawnfluke");
    }

    @Test
    @DisplayName("ETB can protect the caster: prevention shield is applied to a targeted player")
    void hardcastCanTargetPlayer() {
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB prevents exactly the next 3 damage to the targeted player")
    void preventsExactlyNextThreeDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent firstAttacker = addCreatureReady(player1, new AvianChangeling());
        Permanent secondAttacker = addCreatureReady(player1, new AvianChangeling());
        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveCombat(player1);

        harness.assertLife(player2, 19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying only {W}, ETB still applies the prevention shield")
    void evokeAppliesShield() {
        Permanent target = addTargetCreature(player1);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithEvoke(player1, 0, target.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger (prevent + evoke sacrifice)

        assertThat(target.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Evoke: Dawnfluke is sacrificed as it enters")
    void evokeSacrificesSelf() {
        Permanent target = addTargetCreature(player1);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithEvoke(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dawnfluke");
        harness.assertInGraveyard(player1, "Dawnfluke");
    }
}
