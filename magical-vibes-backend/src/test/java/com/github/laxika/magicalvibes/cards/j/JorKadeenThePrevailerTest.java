package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class JorKadeenThePrevailerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has metalcraft static boost effect with +3/+0 for own creatures")
    void hasCorrectEffect() {
        JorKadeenThePrevailer card = new JorKadeenThePrevailer();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) metalcraft.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Base 5/4 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());

        Permanent jor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost to other creatures without metalcraft")
    void noBoostToOtherCreaturesWithoutMetalcraft() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost with only two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Own creatures get +3/+0 with three artifacts")
    void metalcraftBoostsOwnCreatures() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jor Kadeen itself gets +3/+0 with metalcraft")
    void metalcraftBoostsSelf() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent jor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jor Kadeen, the Prevailer"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
