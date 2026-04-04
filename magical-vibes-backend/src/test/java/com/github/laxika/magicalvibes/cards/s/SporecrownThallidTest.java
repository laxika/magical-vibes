package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeathbloomThallid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class SporecrownThallidTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sporecrown Thallid has static boost effect for Fungus and Saproling")
    void hasCorrectStaticEffect() {
        SporecrownThallid card = new SporecrownThallid();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).isEmpty();
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    // ===== Static effect: buffs other Fungus/Saproling you control =====

    @Test
    @DisplayName("Other Fungus creatures you control get +1/+1")
    void buffsOtherFungus() {
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player1, new DeathbloomThallid());

        Permanent deathbloom = findPermanent(player1, "Deathbloom Thallid");

        // Deathbloom Thallid is 3/2 base + 1/1 from Sporecrown = 4/3
        assertThat(gqs.getEffectivePower(gd, deathbloom)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, deathbloom)).isEqualTo(3);
    }

    @Test
    @DisplayName("Saproling tokens get +1/+1")
    void buffsSaprolingTokens() {
        harness.addToBattlefield(player1, new SporecrownThallid());

        // Create Saproling tokens via SaprolingMigration
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SaprolingMigration()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> saprolings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();

        assertThat(saprolings).hasSize(2);
        for (Permanent saproling : saprolings) {
            // Saproling is 1/1 base + 1/1 from Sporecrown = 2/2
            assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Sporecrown Thallid does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new SporecrownThallid());

        Permanent sporecrown = findPermanent(player1, "Sporecrown Thallid");

        // 2/2 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, sporecrown)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sporecrown)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Fungus/non-Saproling creatures")
    void doesNotBuffNonFungusNonSaproling() {
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Fungus creatures")
    void doesNotBuffOpponentFungus() {
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player2, new DeathbloomThallid());

        Permanent opponentFungus = findPermanent(player2, "Deathbloom Thallid");

        // No buff from opponent's Sporecrown Thallid — base 3/2
        assertThat(gqs.getEffectivePower(gd, opponentFungus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentFungus)).isEqualTo(2);
    }

    // ===== Multiple Sporecrown Thallids =====

    @Test
    @DisplayName("Two Sporecrown Thallids buff each other")
    void twoSporecrownBuffEachOther() {
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player1, new SporecrownThallid());

        List<Permanent> sporecrowns = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sporecrown Thallid"))
                .toList();

        assertThat(sporecrowns).hasSize(2);
        for (Permanent sporecrown : sporecrowns) {
            // 2/2 base + 1/1 from the other = 3/3
            assertThat(gqs.getEffectivePower(gd, sporecrown)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, sporecrown)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Two Sporecrown Thallids give +2/+2 to other Fungus")
    void twoSporecrownStackBonuses() {
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player1, new DeathbloomThallid());

        Permanent deathbloom = findPermanent(player1, "Deathbloom Thallid");

        // 3/2 base + 2/2 from two Sporecrowns = 5/4
        assertThat(gqs.getEffectivePower(gd, deathbloom)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, deathbloom)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Sporecrown Thallid leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new SporecrownThallid());
        harness.addToBattlefield(player1, new DeathbloomThallid());

        Permanent deathbloom = findPermanent(player1, "Deathbloom Thallid");

        assertThat(gqs.getEffectivePower(gd, deathbloom)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sporecrown Thallid"));

        assertThat(gqs.getEffectivePower(gd, deathbloom)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, deathbloom)).isEqualTo(2);
    }

    // ===== Helper methods =====

}
