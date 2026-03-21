package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OpponentPoisonedConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViridianBetrayersTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC effect with OpponentPoisonedConditionalEffect wrapping GrantKeywordEffect(INFECT, SELF)")
    void hasCorrectStructure() {
        ViridianBetrayers card = new ViridianBetrayers();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(OpponentPoisonedConditionalEffect.class);
        OpponentPoisonedConditionalEffect conditional =
                (OpponentPoisonedConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) conditional.wrapped();
        assertThat(grant.keywords()).containsExactly(Keyword.INFECT);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional infect — opponent poisoned =====

    @Test
    @DisplayName("Has infect when opponent is poisoned")
    void hasInfectWhenOpponentPoisoned() {
        Permanent betrayers = addToBattlefield(player1, new ViridianBetrayers());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Does NOT have infect when no opponent is poisoned")
    void noInfectWhenOpponentNotPoisoned() {
        Permanent betrayers = addToBattlefield(player1, new ViridianBetrayers());

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Checks opponent for poison, not controller")
    void checksOpponentNotController() {
        Permanent betrayers = addToBattlefield(player1, new ViridianBetrayers());
        // Controller has poison but opponent does not
        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Gains infect dynamically when opponent becomes poisoned")
    void gainsInfectDynamically() {
        Permanent betrayers = addToBattlefield(player1, new ViridianBetrayers());

        // Initially no infect
        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isFalse();

        // Opponent gets poisoned
        gd.playerPoisonCounters.put(player2.getId(), 1);

        // Now has infect
        assertThat(gqs.hasKeyword(gd, betrayers, Keyword.INFECT)).isTrue();
    }

    // ===== Helper =====

    private Permanent addToBattlefield(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
