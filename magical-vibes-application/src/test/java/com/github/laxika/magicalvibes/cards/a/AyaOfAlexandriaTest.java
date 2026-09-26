package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AyaOfAlexandria.class, GrizzlyBears.class})
class AyaOfAlexandriaTest extends BaseCardTest {

    @Test
    @DisplayName("Historic creature combat damage creates a menacing Assassin")
    void historicCreatureCombatDamageCreatesAssassin() {
        Permanent aya = addCreatureReady(player1, new AyaOfAlexandria());
        aya.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        Permanent assassin = findPermanents(player1, "Assassin").stream().findFirst().orElseThrow();
        assertThat(assassin.getCard().getPower()).isEqualTo(1);
        assertThat(assassin.getCard().getToughness()).isEqualTo(1);
        assertThat(assassin.getCard().getSubtypes()).contains(CardSubtype.ASSASSIN);
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Non-historic creature combat damage does not create an Assassin")
    void nonHistoricCreatureCombatDamageDoesNotCreateAssassin() {
        addCreatureReady(player1, new AyaOfAlexandria());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
    }
}
