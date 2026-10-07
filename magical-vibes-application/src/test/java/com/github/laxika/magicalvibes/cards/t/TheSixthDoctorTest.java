package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.a.AnUnearthlyChild;
import com.github.laxika.magicalvibes.cards.c.ClaraOswald;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSixthDoctor.class, AdelizTheCinderWind.class, GrizzlyBears.class, Spellbook.class,
        SolRing.class, AnUnearthlyChild.class, ClaraOswald.class})
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

        harness.castFromHand(player1, new Spellbook(), "{0}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spellbook")).hasSize(1);
    }

    @Test
    void doesNotCopyNonhistoricSpells() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    private void castHistoricCreature() {
        harness.castFromHand(player1, new AdelizTheCinderWind(), "{1}{U}{R}");
        resolveAllTriggers();
    }

    @Test
    void copiesANonlegendaryArtifactAsAToken() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2)
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void copiesANonlegendarySagaAndBothChapterAbilitiesResolve() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.setLibrary(player1, List.of(new TheSixthDoctor(), new TheSixthDoctor()));
        harness.castFromHand(player1, new AnUnearthlyChild(), "{1}{U}{U}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "An Unearthly Child")).hasSize(2)
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void nonhistoricSpellDoesNotConsumeTheHistoricTrigger() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
    }

    @Test
    void copiesHistoricSpellAfterTheDoctorWasCastEarlierThatTurn() {
        harness.castFromHand(player1, new TheSixthDoctor(), "{4}{G}{U}");
        resolveAllTriggers();
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
    }

    @Test
    void aNewDoctorCanTriggerEvenIfAnEarlierDoctorAlreadyTriggeredThisTurn() {
        Permanent doctor = addCreatureReady(player1, new TheSixthDoctor());
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(doctor);
        addCreatureReady(player1, new TheSixthDoctor());
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(4);
    }

    @Test
    void clarasAdditionalTriggerCannotExceedTheOncePerTurnLimit() {
        addCreatureReady(player1, new TheSixthDoctor());
        addCreatureReady(player1, new ClaraOswald());
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
    }

    @Test
    void doesNotCopyAnOpponentsHistoricSpell() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Sol Ring")).hasSize(1);
        assertThat(findPermanents(player1, "Sol Ring")).isEmpty();
    }

    @Test
    void removingTheDoctorDoesNotStopItsPendingCopyAbility() {
        Permanent doctor = addCreatureReady(player1, new TheSixthDoctor());
        harness.castFromHand(player1, new SolRing(), "{1}");
        gd.playerBattlefields.get(player1.getId()).remove(doctor);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
    }

    @Test
    void canTriggerAgainOnALaterTurn() {
        addCreatureReady(player1, new TheSixthDoctor());
        harness.setLibrary(player1, List.of(new SolRing(), new SolRing()));
        harness.setLibrary(player2, List.of(new SolRing(), new SolRing()));
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(4);
    }
}
