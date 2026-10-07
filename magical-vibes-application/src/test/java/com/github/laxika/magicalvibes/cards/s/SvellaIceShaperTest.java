package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SvellaIceShaper.class, CounselOfTheSoratami.class, Forest.class,
        GrizzlyBears.class, LlanowarElves.class, Mountain.class, VillageRites.class})
class SvellaIceShaperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a snow Icy Manalith that produces snow mana")
    void createsSnowIcyManalith() {
        Permanent svella = addSvella();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(svella), 0, null, null);
        harness.passBothPriorities();

        Permanent manalith = findPermanent(player1, "Icy Manalith");
        int manalithIndex = gd.playerBattlefields.get(player1.getId()).indexOf(manalith);
        harness.activateAbility(player1, manalithIndex, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Looks at exactly four cards and offers a nonland spell for free")
    void looksAtFourCardsAndCastsSpellForFree() {
        Permanent svella = addSvella();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(
                counsel, new Forest(), new Mountain(), new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(svella), 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);

        int counselIndex = search.params().cards().indexOf(counsel);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, counselIndex);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("May decline the spell and puts all four cards on the library bottom")
    void mayDeclineFreeCast() {
        Permanent svella = addSvella();
        harness.setLibrary(player1, List.of(
                new CounselOfTheSoratami(), new Forest(), new Mountain(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(svella), 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("A spell cast for free still requires its creature sacrifice cost")
    void paysMandatoryAdditionalCost() {
        Permanent svella = addSvella();
        VillageRites rites = new VillageRites();
        List<Card> library = List.of(rites, new Forest(), new Mountain(), new Forest());
        harness.setLibrary(player1, library);
        activateFreeCast(svella);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(library);
        harness.handlePermanentChosen(player1, svella.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(svella);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rites, svella.getCard());
    }

    @Test
    @DisplayName("Can cast a creature from a library with fewer than four cards")
    void castsFromShortLibrary() {
        Permanent svella = addSvella();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        activateFreeCast(svella);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCard()).isSameAs(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Land-only cards are bottomed while the untouched fifth card stays on top")
    void landOnlyCardsGoToBottom() {
        Permanent svella = addSvella();
        List<Card> lands = List.of(new Forest(), new Mountain(), new Forest(), new Mountain());
        GrizzlyBears fifth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(lands.get(0), lands.get(1), lands.get(2), lands.get(3), fifth));
        activateFreeCast(svella);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(lands);
    }

    @Test
    @DisplayName("An empty library finishes the ability without offering a choice")
    void emptyLibraryFinishesNormally() {
        Permanent svella = addSvella();
        harness.setLibrary(player1, List.of());
        activateFreeCast(svella);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents both tap abilities")
    void summoningSicknessPreventsActivation() {
        Permanent svella = addSvella();
        svella.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(svella), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(svella), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Icy Manalith")).isZero();
    }

    private void activateFreeCast(Permanent svella) {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(svella), 1, null, null);
        harness.passBothPriorities();
    }

    private Permanent addSvella() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new SvellaIceShaper());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
