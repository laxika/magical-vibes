package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EzioBrashNovice;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AyaOfAlexandria.class, GrizzlyBears.class, EzioBrashNovice.class,
        Juggernaut.class, Ornithopter.class})
class AyaOfAlexandriaTest extends BaseCardTest {

    @Test
    @DisplayName("Historic creature combat damage creates a menacing Assassin")
    void historicCreatureCombatDamageCreatesAssassin() {
        Permanent aya = addCreatureReady(player1, new AyaOfAlexandria());
        aya.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        Permanent assassin = findPermanent(player1, "Assassin");
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

    @Test
    void anotherLegendaryCreatureCreatesOneAssassinRegardlessOfDamageAmount() {
        addCreatureReady(player1, new AyaOfAlexandria());
        Permanent ezio = addCreatureReady(player1, new EzioBrashNovice());
        ezio.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).hasSize(1);
        harness.assertLife(player2, 19);
    }

    @Test
    void nonLegendaryArtifactCreatureCreatesAssassin() {
        addCreatureReady(player1, new AyaOfAlexandria());
        Permanent juggernaut = addCreatureReady(player1, new Juggernaut());
        juggernaut.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).hasSize(1);
        harness.assertLife(player2, 15);
    }

    @Test
    void eachHistoricCreatureDealingDamageCreatesItsOwnAssassin() {
        Permanent aya = addCreatureReady(player1, new AyaOfAlexandria());
        Permanent ezio = addCreatureReady(player1, new EzioBrashNovice());
        aya.setAttacking(true);
        ezio.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).hasSize(2);
        harness.assertLife(player2, 15);
        harness.assertLife(player1, 24);
        for (Permanent assassin : findPermanents(player1, "Assassin")) {
            assertThat(assassin.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(assassin.isTapped()).isFalse();
            assertThat(assassin.isAttacking()).isFalse();
        }
    }

    @Test
    void opponentsHistoricCreatureDoesNotTriggerAya() {
        addCreatureReady(player1, new AyaOfAlexandria());
        Permanent ezio = addCreatureReady(player2, new EzioBrashNovice());
        ezio.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
        assertThat(findPermanents(player2, "Assassin")).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    void historicCreatureWithZeroPowerDoesNotTrigger() {
        addCreatureReady(player1, new AyaOfAlexandria());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        ornithopter.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void combatDamageToCreaturesDoesNotCreateAssassin() {
        Permanent aya = addCreatureReady(player1, new AyaOfAlexandria());
        aya.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 24);
    }
}
