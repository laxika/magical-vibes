package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GargoyleFlock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyranidHarridan.class, GargoyleFlock.class, GrizzlyBears.class})
class TyranidHarridanTest extends BaseCardTest {

    @Test
    @DisplayName("Tyranid combat damage creates a flying blue Tyranid Gargoyle")
    void tyranidCombatDamageCreatesGargoyle() {
        addCreatureReady(player1, new TyranidHarridan()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Tyranid Gargoyle");
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.TYRANID, CardSubtype.GARGOYLE);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Another Tyranid dealing combat damage also creates a Gargoyle")
    void anotherTyranidCombatDamageCreatesGargoyle() {
        addCreatureReady(player1, new TyranidHarridan());
        addCreatureReady(player1, new GargoyleFlock()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Tyranid Gargoyle")).hasSize(1);
    }

    @Test
    @DisplayName("A non-Tyranid dealing combat damage does not create a Gargoyle")
    void nonTyranidCombatDamageDoesNotCreateGargoyle() {
        addCreatureReady(player1, new TyranidHarridan());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Tyranid Gargoyle")).isEmpty();
    }
}
