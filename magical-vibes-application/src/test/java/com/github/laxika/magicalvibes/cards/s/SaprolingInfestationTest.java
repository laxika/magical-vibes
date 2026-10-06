package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdentSoldier.class, GnarlidPack.class, SaprolingInfestation.class})
class SaprolingInfestationTest extends BaseCardTest {

    @Test
    void createsSaprolingWhenAnyPlayerCastsKickedSpell() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.setHand(player2, List.of(new ArdentSoldier()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castKickedCreature(player2, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
    }

    @Test
    void doesNotCreateSaprolingForNonKickedSpell() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void createsSaprolingForControllersKickedSpellBeforeSpellResolves() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Ardent Soldier");
        assertThat(countPermanents(player2, "Saproling")).isZero();

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ardent Soldier");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    void eachInfestationCreatesTokenForItsOwnController() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.addToBattlefield(player2, new SaprolingInfestation());
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        assertThat(countPermanents(player2, "Saproling")).isEqualTo(1);
    }

    @Test
    void createsSeparateSaprolingTriggersForEachMultikickerPayment() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}", "{1}{G}"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Gnarlid Pack");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Gnarlid Pack");

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Gnarlid Pack");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }
}
