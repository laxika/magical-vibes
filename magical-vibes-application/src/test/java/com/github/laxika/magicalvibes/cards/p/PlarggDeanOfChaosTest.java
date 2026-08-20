package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
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

    private Permanent castAugusta() {
        harness.setHand(player1, List.of(new PlarggDeanOfChaos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        return findPermanent(player1, "Augusta, Dean of Order");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
