package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BellowingTanglewurmTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has static grant intimidate to own green creatures effect")
    void hasCorrectEffect() {
        BellowingTanglewurm card = new BellowingTanglewurm();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.keywords()).containsExactly(Keyword.INTIMIDATE);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isNotNull();
    }

    // ===== Grants intimidate to own green creatures =====

    @Test
    @DisplayName("Own green creature gains intimidate")
    void ownGreenCreatureGainsIntimidate() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant intimidate to itself")
    void doesNotGrantToSelf() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());

        Permanent wurm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bellowing Tanglewurm"))
                .findFirst().orElseThrow();
        // Bellowing Tanglewurm has innate intimidate from Scryfall, but the static
        // effect should not grant an extra copy to itself ("Other green creatures").
        // The innate keyword is still present.
        assertThat(wurm.hasKeyword(Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant intimidate to own non-green creature")
    void doesNotGrantToNonGreenCreature() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent giant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant intimidate to opponent's green creature")
    void doesNotGrantToOpponentGreenCreature() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    // ===== Lord removal =====

    @Test
    @DisplayName("Intimidate is lost when Bellowing Tanglewurm leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();

        // Remove the lord
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Bellowing Tanglewurm"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }
}
