package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.UginsConjurant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinaleOfDevastation.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class,
        UginsConjurant.class})
class FinaleOfDevastationTest extends BaseCardTest {

    @Test
    @DisplayName("Searches either zone for a creature with mana value at most X")
    void searchesLibraryAndGraveyardWithinX() {
        Card libraryBear = new GrizzlyBears();
        Card libraryAirElemental = new AirElemental();
        Card graveyardBear = new GrizzlyBears();
        Card graveyardAirElemental = new AirElemental();
        setLibrary(libraryBear, libraryAirElemental);
        harness.setGraveyard(player1, List.of(graveyardBear, graveyardAirElemental));

        castFinale(2);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(libraryBear.getId(), graveyardBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardBear.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == graveyardBear);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).doesNotContain(graveyardBear);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(libraryBear, libraryAirElemental);
    }

    @Test
    @DisplayName("At X=10, gives all controlled creatures +X/+X and haste")
    void givesBonusAtTen() {
        Card libraryCreature = new LlanowarElves();
        setLibrary(libraryCreature);
        harness.addToBattlefield(player1, new GrizzlyBears());

        castFinale(10);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(libraryCreature.getId()));

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == libraryCreature)
                .findFirst().orElseThrow();
        assertThat(elves.getEnteredFromZone()).isEqualTo(Zone.LIBRARY);
        assertThat(elves.getEnteredFromGraveyardOwnerId()).isNull();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(12);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bears)).isEqualTo(12);
        assertThat(harness.getGameQueryService().hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, elves)).isEqualTo(11);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, elves)).isEqualTo(11);
        assertThat(harness.getGameQueryService().hasKeyword(gd, elves, Keyword.HASTE)).isTrue();
    }

    @Test
    void noBonusBelowTen() {
        Card creature = new GrizzlyBears();
        setLibrary(creature);
        castFinale(9);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        GameData gd = harness.getGameData();
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, permanent)).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
    }

    @Test
    void bonusWithoutFindingCreatureOnlyAffectsExistingOwnCreaturesAndExpires() {
        setLibrary();
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFinale(12);

        GameData gd = harness.getGameData();
        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent opposing = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, own)).isEqualTo(14);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, own)).isEqualTo(14);
        assertThat(harness.getGameQueryService().hasKeyword(gd, own, Keyword.HASTE)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, opposing, Keyword.HASTE)).isFalse();

        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent later = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, later)).isEqualTo(1);
        assertThat(harness.getGameQueryService().hasKeyword(gd, later, Keyword.HASTE)).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, own, Keyword.HASTE)).isFalse();
    }

    @Test
    void bonusStillAppliesWhenLibrarySearchFindsNothingByChoice() {
        setLibrary(new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFinale(10);
        harness.handleMultipleCardsChosen(player1, List.of());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(harness.getGameQueryService().hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureWithXInItsCostGetsBonusBeforeStateBasedActions() {
        Card creature = new UginsConjurant();
        setLibrary(creature);
        castFinale(10);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Ugin's Conjurant");
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, permanent)).isEqualTo(10);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, permanent)).isEqualTo(10);
        assertThat(harness.getGameQueryService().hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    private void castFinale(int xValue) {
        harness.setHand(player1, List.of(new FinaleOfDevastation()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 2);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
