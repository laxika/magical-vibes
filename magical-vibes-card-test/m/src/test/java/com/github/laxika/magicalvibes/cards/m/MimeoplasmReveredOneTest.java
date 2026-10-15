package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlteredEgo;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MimeoplasmReveredOne.class, GrizzlyBears.class, GiantSpider.class, HillGiant.class,
        Pacifism.class, AlteredEgo.class})
class MimeoplasmReveredOneTest extends BaseCardTest {

    private void castMimeoplasm(int xValue) {
        harness.setHand(player1, new ArrayList<>(List.of(new MimeoplasmReveredOne())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    private Permanent mimeoplasm() {
        return findPermanent(player1, "Mimeoplasm, Revered One");
    }

    @Test
    @DisplayName("Exiles up to X creature cards and gets three counters for each chosen card")
    void exilesUpToXCreaturesWithCounters() {
        GrizzlyBears bears = new GrizzlyBears();
        GiantSpider spider = new GiantSpider();
        HillGiant giant = new HillGiant();
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, spider, giant, pacifism)));

        castMimeoplasm(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), spider.getId(), giant.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), spider.getId()));

        Permanent mimeoplasm = mimeoplasm();
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, mimeoplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mimeoplasm)).isEqualTo(6);
        assertThat(gd.getCardsExiledByPermanent(mimeoplasm.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(bears.getId(), spider.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(giant.getId(), pacifism.getId());
    }

    @Test
    @DisplayName("Becomes a 0/0 copy of a creature card exiled with it and keeps this ability")
    void copiesExiledCreatureAndKeepsAbility() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        castMimeoplasm(1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent mimeoplasm = mimeoplasm();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(mimeoplasm.getCard().getPower()).isZero();
        assertThat(mimeoplasm.getCard().getToughness()).isZero();
        assertThat(mimeoplasm.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mimeoplasm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimeoplasm)).isEqualTo(3);
    }

    @Test
    void mayExileFewerThanXCards() {
        GrizzlyBears bears = new GrizzlyBears();
        GiantSpider spider = new GiantSpider();
        harness.setGraveyard(player1, List.of(bears, spider));

        castMimeoplasm(2);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(mimeoplasm().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    void choosingNoCardsLeavesZeroToughnessAndDies() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castMimeoplasm(1);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Mimeoplasm, Revered One");
        harness.assertInGraveyard(player1, "Mimeoplasm, Revered One");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void zeroXDoesNotExileCreaturesAndDies() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castMimeoplasm(0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Mimeoplasm, Revered One");
        harness.assertInGraveyard(player1, "Mimeoplasm, Revered One");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void canCopyAgainAndGainTheNewCopiesKeywords() {
        GrizzlyBears bears = new GrizzlyBears();
        GiantSpider spider = new GiantSpider();
        harness.setGraveyard(player1, List.of(bears, spider));
        castMimeoplasm(2);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), spider.getId()));
        Permanent source = mimeoplasm();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId(), Zone.EXILE);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, spider.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(source.getCard().getName()).isEqualTo("Giant Spider");
        assertThat(gqs.hasKeyword(gd, source, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
    }

    @Test
    void enteringCopyUsesItsSpellXAndRetainsTheCopyAbilityAfterActivation() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castMimeoplasm(1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent original = mimeoplasm();

        GrizzlyBears copiedBears = new GrizzlyBears();
        GiantSpider copiedSpider = new GiantSpider();
        harness.setGraveyard(player2, List.of(copiedBears, copiedSpider));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new AlteredEgo()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player2, 0, 2);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, original.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player2, List.of(copiedBears.getId(), copiedSpider.getId()));
        Permanent copy = findPermanent(player2, "Mimeoplasm, Revered One");
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, copiedBears.getId(), Zone.EXILE);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, copiedSpider.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(copy.getCard().getName()).isEqualTo("Giant Spider");
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(8);
    }
}
