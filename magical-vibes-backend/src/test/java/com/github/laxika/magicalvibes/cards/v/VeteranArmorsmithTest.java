package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeteranArmorsmithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Veteran Armorsmith has static boost effect for Soldiers with +0/+1")
    void hasCorrectStaticEffect() {
        VeteranArmorsmith card = new VeteranArmorsmith();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(0);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).isEmpty();
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    // ===== Static effect: buffs other Soldiers you control =====

    @Test
    @DisplayName("Other Soldier creatures you control get +0/+1")
    void buffsOtherSoldiersYouControl() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent soldier = findPermanent(player1, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Veteran Armorsmith does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());

        Permanent armorsmith = findPermanent(player1, "Veteran Armorsmith");

        assertThat(gqs.getEffectivePower(gd, armorsmith)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, armorsmith)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Soldier creatures")
    void doesNotBuffOpponentSoldiers() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent opponentSoldier = findPermanent(player2, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(1);
    }

    // ===== Multiple Veteran Armorsmiths =====

    @Test
    @DisplayName("Two Veteran Armorsmiths buff each other with +0/+1")
    void twoArmorsmithsBuffEachOther() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new VeteranArmorsmith());

        List<Permanent> armorsmiths = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Veteran Armorsmith"))
                .toList();

        assertThat(armorsmiths).hasSize(2);
        for (Permanent armorsmith : armorsmiths) {
            assertThat(gqs.getEffectivePower(gd, armorsmith)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, armorsmith)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Two Veteran Armorsmiths give +0/+2 to other Soldiers")
    void twoArmorsmithsStackBonuses() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent soldier = findPermanent(player1, "Elite Vanguard");

        // 2/1 base + 0/2 from two armorsmiths = 2/3
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Veteran Armorsmith leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent soldier = findPermanent(player1, "Elite Vanguard");

        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Veteran Armorsmith"));

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus applies when Veteran Armorsmith resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent soldier = findPermanent(player1, "Elite Vanguard");
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);

        harness.setHand(player1, List.of(new VeteranArmorsmith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
