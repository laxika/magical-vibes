package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuriokEdgewrightTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has metalcraft double strike static effect")
    void hasMetalcraftDoubleStrikeEffect() {
        AuriokEdgewright card = new AuriokEdgewright();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect grant = (GrantKeywordEffect) metalcraft.wrapped();
        assertThat(grant.keywords()).containsExactly(Keyword.DOUBLE_STRIKE);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Metalcraft behavior =====

    @Test
    @DisplayName("No double strike with zero artifacts")
    void noDoubleStrikeWithZeroArtifacts() {
        harness.addToBattlefield(player1, new AuriokEdgewright());

        Permanent edgewright = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No double strike with two artifacts")
    void noDoubleStrikeWithTwoArtifacts() {
        harness.addToBattlefield(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent edgewright = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Edgewright"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with exactly three artifacts")
    void hasDoubleStrikeWithThreeArtifacts() {
        harness.addToBattlefield(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent edgewright = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Edgewright"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike when artifact count drops below three")
    void losesDoubleStrikeWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new AuriokEdgewright());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent edgewright = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Edgewright"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new AuriokEdgewright());
        // Opponent has 3 artifacts, controller has 0
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent edgewright = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Edgewright"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, edgewright, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
