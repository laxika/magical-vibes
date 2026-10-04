package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuroraAwakener.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        Shock.class, SoulWarden.class, Pacifism.class, GrafdiggersCage.class})
class AuroraAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Vivid reveals until it finds as many permanents as colors among your permanents")
    void revealsUntilRequiredNumberOfPermanents() {
        harness.addToBattlefield(player1, new AirElemental());
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, bears, forest));

        castAuroraAwakener();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).extracting(Card::getId)
                .containsExactly(shock.getId(), bears.getId(), forest.getId());
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), forest.getId());
    }

    @Test
    @DisplayName("Choosing any number puts the selected permanents onto the battlefield and bottoms the rest")
    void choosesAnyNumberAndBottomsTheRest() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, bears, forest));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), forest.getId());
    }

    @Test
    @DisplayName("Choosing no permanents leaves every revealed card on the library bottom")
    void mayChooseNoPermanents() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, bears));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), bears.getId());
    }

    @Test
    @DisplayName("If no permanent is found, the whole revealed library is put on the bottom")
    void noPermanentFound() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        castAuroraAwakener();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(shock.getId());
    }

    @Test
    @DisplayName("Unrevealed cards stay on top when unchosen revealed cards go to the bottom")
    void preservesUnrevealedCards() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, bears, forest));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(forest.getId());
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3)).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), bears.getId());
    }

    @Test
    @DisplayName("All matching permanents can be chosen even when the library contains fewer than X")
    void choosesPermanentsWhenLibraryRunsOut() {
        harness.addToBattlefield(player1, new AirElemental());
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, forest));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(forest.getId());
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(shock.getId());
    }

    @Test
    @DisplayName("Opponent colors and duplicate green permanents do not increase X")
    void countsOnlyDistinctControlledColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AirElemental());
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));

        castAuroraAwakener();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).extracting(Card::getId).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(forest.getId());
    }

    @Test
    @DisplayName("A creature entering with Soul Warden triggers it even when revealed first")
    void selectedPermanentsEnterSimultaneously() {
        harness.addToBattlefield(player1, new AirElemental());
        Card bears = new GrizzlyBears();
        Card warden = new SoulWarden();
        harness.setLibrary(player1, List.of(bears, warden));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), warden.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Colors are counted when the entry trigger resolves")
    void countsColorsAtResolution() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        harness.castFromHand(player1, new AuroraAwakener(), "{6}{G}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new AirElemental());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).extracting(Card::getId)
                .containsExactly(bears.getId(), forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), forest.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library reveals nothing and needs no selection")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAuroraAwakener();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Aura put onto the battlefield can attach to an existing creature")
    void choosesAuraAttachment() {
        Card aura = new Pacifism();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(aura));

        castAuroraAwakener();
        var awakener = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AuroraAwakener)
                .findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, awakener.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
                    assertThat(permanent.getAttachedTo()).isEqualTo(awakener.getId());
                });
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents chosen creatures entering and they go to the bottom")
    void respectsLibraryEntryRestriction() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(forest.getId(), bears.getId());
    }

    private void castAuroraAwakener() {
        harness.castFromHand(player1, new AuroraAwakener(), "{6}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
