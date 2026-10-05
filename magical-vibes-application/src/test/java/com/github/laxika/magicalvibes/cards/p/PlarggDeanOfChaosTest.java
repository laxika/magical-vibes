package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlarggDeanOfChaos.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class PlarggDeanOfChaosTest extends BaseCardTest {

    @Test
    void tapsDiscardsAndDraws() {
        Permanent plargg = addCreatureReady(player1, new PlarggDeanOfChaos());
        Card discard = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, indexOf(plargg), 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(plargg.isTapped()).isTrue();
    }

    @Test
    void revealsUntilMatchingCardAndMayCastItForFree() {
        Permanent plargg = addCreatureReady(player1, new PlarggDeanOfChaos());
        Card land = new Forest();
        Card hit = new GrizzlyBears();
        Card remaining = new HillGiant();
        harness.setLibrary(player1, List.of(land, hit, remaining));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(plargg), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, remaining);
    }

    @Test
    void augustaGivesToughnessToUntappedAndPowerToTappedCreatures() {
        castAugusta();
        Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
        Permanent untapped = addCreatureReady(player1, new GrizzlyBears());
        tapped.tap();

        assertThat(gqs.getEffectivePower(gd, tapped)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tapped)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, untapped)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untapped)).isEqualTo(3);
    }

    @Test
    void augustaUntapsThenLetsControllerTapAnyNumberOfCreatures() {
        Permanent augusta = castAugusta();
        Permanent selected = addCreatureReady(player1, new GrizzlyBears());
        Permanent notSelected = addCreatureReady(player1, new HillGiant());
        selected.tap();
        notSelected.tap();

        declareAttackers(player1, List.of(indexOf(augusta)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                augusta.getId(), selected.getId(), notSelected.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId()));

        assertThat(augusta.isTapped()).isFalse();
        assertThat(selected.isTapped()).isTrue();
        assertThat(notSelected.isTapped()).isFalse();
    }

    @Test
    void decliningFreeCastBottomsEveryRevealedCardAfterUnrevealedCards() {
        Permanent plargg = addCreatureReady(player1, new PlarggDeanOfChaos());
        Card land = new Forest();
        Card hit = new GrizzlyBears();
        Card remaining = new HillGiant();
        harness.setLibrary(player1, List.of(land, hit, remaining));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(plargg), 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, hit);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void skipsLegendaryCardsAndCardsWithManaValueGreaterThanThree() {
        Permanent plargg = addCreatureReady(player1, new PlarggDeanOfChaos());
        Card legendary = new PlarggDeanOfChaos();
        Card expensive = new HillGiant();
        Card hit = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(legendary, expensive, hit, remaining));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(plargg), 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(legendary, expensive);
    }

    @Test
    void noEligibleCardReturnsEntireLibraryWithoutCasting() {
        Permanent plargg = addCreatureReady(player1, new PlarggDeanOfChaos());
        Card land = new Forest();
        Card legendary = new PlarggDeanOfChaos();
        Card expensive = new HillGiant();
        harness.setLibrary(player1, List.of(land, legendary, expensive));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(plargg), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, legendary, expensive);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void augustaDoesNotBoostHerselfOrOpponentsCreatures() {
        Permanent augusta = castAugusta();
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, augusta)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
        augusta.tap();
        opponent.tap();
        assertThat(gqs.getEffectivePower(gd, augusta)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    void augustaTriggersWhenAnotherCreatureAttacksAndAllowsTappingZero() {
        Permanent augusta = castAugusta();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new HillGiant());
        augusta.tap();
        opponent.tap();

        declareAttackers(player1, List.of(indexOf(attacker)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(augusta.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(opponent.isTapped()).isTrue();
    }

    private Permanent castAugusta() {
        harness.setHand(player1, List.of(new PlarggDeanOfChaos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        Permanent augusta = findPermanent(player1, "Augusta, Dean of Order");
        augusta.setSummoningSick(false);
        return augusta;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
