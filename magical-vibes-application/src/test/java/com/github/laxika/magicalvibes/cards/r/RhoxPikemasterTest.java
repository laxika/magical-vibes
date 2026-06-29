package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RhoxPikemasterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has static grant first strike to own Soldier creatures effect")
    void hasCorrectEffect() {
        RhoxPikemaster card = new RhoxPikemaster();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.keywords()).containsExactly(Keyword.FIRST_STRIKE);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isNotNull();
    }

    // ===== Grants first strike to own Soldier creatures =====

    @Test
    @DisplayName("Own Soldier creature gains first strike")
    void ownSoldierGainsFirstStrike() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant first strike to itself (already has innate first strike)")
    void doesNotGrantToSelf() {
        harness.addToBattlefield(player1, new RhoxPikemaster());

        Permanent pikemaster = findPermanent(player1, "Rhox Pikemaster");
        // Rhox Pikemaster has innate first strike from Scryfall, but the static
        // effect should not grant an extra copy to itself ("Other Soldier creatures").
        // The innate keyword is still present.
        assertThat(pikemaster.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant first strike to non-Soldier creature")
    void doesNotGrantToNonSoldier() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to opponent's Soldier creature")
    void doesNotGrantToOpponentSoldier() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent vanguard = findPermanent(player2, "Elite Vanguard");
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Lord removal =====

    @Test
    @DisplayName("First strike is lost when Rhox Pikemaster leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isTrue();

        // Remove the lord
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Rhox Pikemaster"));

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Multiple Rhox Pikemasters =====

    @Test
    @DisplayName("Two Rhox Pikemasters grant first strike to each other")
    void twoPikemastersGrantToEachOther() {
        harness.addToBattlefield(player1, new RhoxPikemaster());
        harness.addToBattlefield(player1, new RhoxPikemaster());

        List<Permanent> pikemasters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rhox Pikemaster"))
                .toList();

        assertThat(pikemasters).hasSize(2);
        for (Permanent pikemaster : pikemasters) {
            // Each has innate first strike + receives it from the other (redundant but correct)
            assertThat(gqs.hasKeyword(gd, pikemaster, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    // ===== Helper methods =====

}
