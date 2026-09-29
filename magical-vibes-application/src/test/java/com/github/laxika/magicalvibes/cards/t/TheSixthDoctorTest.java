package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSixthDoctor.class, AdelizTheCinderWind.class, GrizzlyBears.class, Spellbook.class})
class TheSixthDoctorTest extends BaseCardTest {

    @Test
    void copiesOnlyTheFirstHistoricSpellEachTurnAsANonlegendaryToken() {
        addCreatureReady(player1, new TheSixthDoctor());
        castHistoricCreature();

        List<Permanent> adelizes = findPermanents(player1, "Adeliz, the Cinder Wind");
        assertThat(adelizes).hasSize(2);
        Permanent token = adelizes.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spellbook")).hasSize(1);
    }

    @Test
    void doesNotCopyNonhistoricSpells() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    private void castHistoricCreature() {
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
