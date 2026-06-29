package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KwendePrideOfFemerefTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static effect granting double strike to creatures with first strike")
    void hasCorrectEffects() {
        KwendePrideOfFemeref card = new KwendePrideOfFemeref();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isZero();
        assertThat(boost.toughnessBoost()).isZero();
        assertThat(boost.grantedKeywords()).containsExactly(Keyword.DOUBLE_STRIKE);
        assertThat(boost.scope()).isEqualTo(GrantScope.ALL_OWN_CREATURES);
        assertThat(boost.filter()).isInstanceOf(PermanentHasKeywordPredicate.class);
        PermanentHasKeywordPredicate filter = (PermanentHasKeywordPredicate) boost.filter();
        assertThat(filter.keyword()).isEqualTo(Keyword.FIRST_STRIKE);
    }

    // ===== Static ability: creatures with first strike gain double strike =====

    @Test
    @DisplayName("Own creature with first strike gains double strike")
    void ownFirstStrikeCreatureGainsDoubleStrike() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Knight"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Own creature without first strike does not gain double strike")
    void creatureWithoutFirstStrikeDoesNotGainDoubleStrike() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creature with first strike does not gain double strike")
    void opponentFirstStrikeCreatureNotAffected() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        harness.addToBattlefield(player2, new BenalishKnight());

        Permanent knight = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Knight"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is lost when Kwende leaves the battlefield")
    void doubleStrikeLostWhenKwendeRemoved() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Knight"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();

        // Remove Kwende
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kwende, Pride of Femeref"));

        // Knight should revert to just first strike
        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Kwende itself has double strike but is not affected by its own static ability (no first strike)")
    void kwendeItselfNotAffectedByOwnAbility() {
        harness.addToBattlefield(player1, new KwendePrideOfFemeref());

        Permanent kwende = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kwende, Pride of Femeref"))
                .findFirst().orElseThrow();

        // Kwende has double strike intrinsically, not from its own static ability
        assertThat(gqs.hasKeyword(gd, kwende, Keyword.DOUBLE_STRIKE)).isTrue();
        // Kwende does NOT have first strike (it has double strike)
        assertThat(gqs.hasKeyword(gd, kwende, Keyword.FIRST_STRIKE)).isFalse();
    }
}
