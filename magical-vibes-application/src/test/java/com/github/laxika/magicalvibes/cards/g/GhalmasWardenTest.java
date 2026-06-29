package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhalmasWardenTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has metalcraft static boost effect with +2/+2")
    void hasCorrectEffect() {
        GhalmasWarden card = new GhalmasWarden();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) metalcraft.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 2/4 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        harness.addToBattlefield(player1, new GhalmasWarden());

        Permanent warden = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base 2/4 with two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent warden = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ghalma's Warden"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Gets +2/+2 (becomes 4/6) with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        harness.addToBattlefield(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent warden = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ghalma's Warden"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(6);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent warden = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ghalma's Warden"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new GhalmasWarden());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent warden = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }
}
