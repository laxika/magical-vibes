package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.c.CutDown;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OnduWarCleric;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowRitePriest.class, OnduWarCleric.class, FeralShadow.class, GrizzlyBears.class,
        CutDown.class, ArtificialEvolution.class, BoggartShenanigans.class})
class ShadowRitePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Other Clerics you control get +1/+1")
    void boostsOtherClericsYouControl() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        int clericPower = gqs.getEffectivePower(gd, cleric);
        int clericToughness = gqs.getEffectiveToughness(gd, cleric);
        Permanent nonCleric = addCreatureReady(player1, new GrizzlyBears());
        int nonClericPower = gqs.getEffectivePower(gd, nonCleric);
        Permanent opponentCleric = addCreatureReady(player2, new OnduWarCleric());
        int opponentClericPower = gqs.getEffectivePower(gd, opponentCleric);
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        int priestPower = priest.getCard().getPower();
        int priestToughness = priest.getCard().getToughness();

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(clericPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(clericToughness + 1);
        assertThat(gqs.getEffectivePower(gd, priest)).isEqualTo(priestPower);
        assertThat(gqs.getEffectiveToughness(gd, priest)).isEqualTo(priestToughness);
        assertThat(gqs.getEffectivePower(gd, nonCleric)).isEqualTo(nonClericPower);
        assertThat(gqs.getEffectivePower(gd, opponentCleric)).isEqualTo(opponentClericPower);
    }

    @Test
    @DisplayName("Sacrificing another Cleric searches for a black creature and puts it onto the battlefield")
    void sacrificesAnotherClericAndSearchesForBlackCreature() {
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new FeralShadow()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(priest), null, null);

        assertThat(priest.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ondu War Cleric");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Feral Shadow");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Feral Shadow");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cleric);
    }

    @Test
    @DisplayName("The ability cannot be activated without another Cleric to sacrifice")
    void cannotActivateWithoutAnotherCleric() {
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(priest), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoPriestsBoostEachOtherAndBoostEndsWhenOneIsSacrificed() {
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        Permanent otherPriest = addCreatureReady(player1, new ShadowRitePriest());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThat(gqs.getEffectivePower(gd, priest)).isEqualTo(priest.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, priest)).isEqualTo(priest.getCard().getToughness() + 1);
        assertThat(gqs.getEffectivePower(gd, otherPriest)).isEqualTo(otherPriest.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherPriest)).isEqualTo(otherPriest.getCard().getToughness() + 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherPriest);
        assertThat(gqs.getEffectivePower(gd, priest)).isEqualTo(priest.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, priest)).isEqualTo(priest.getCard().getToughness());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsCleric() {
        addCreatureReady(player1, new ShadowRitePriest());
        addCreatureReady(player2, new ShadowRitePriest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ShadowRitePriest());
        addCreatureReady(player1, new ShadowRitePriest());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutTwoBlackMana() {
        Permanent priest = addCreatureReady(player1, new ShadowRitePriest());
        Permanent otherPriest = addCreatureReady(player1, new ShadowRitePriest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(priest.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherPriest);
    }

    @Test
    void foundCreatureEntersUntappedWithoutPayingItsManaCost() {
        addCreatureReady(player1, new ShadowRitePriest());
        addCreatureReady(player1, new ShadowRitePriest());
        ShadowRitePriest foundCard = new ShadowRitePriest();
        harness.setLibrary(player1, List.of(foundCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent foundPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(foundCard.getId()))
                .findFirst().orElseThrow();
        assertThat(foundPermanent.isTapped()).isFalse();
        assertThat(foundPermanent.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Shadow-Rite Priest")).isEqualTo(2);
    }

    @Test
    void searchExcludesBlackNoncreaturesAndMayFailToFind() {
        addCreatureReady(player1, new ShadowRitePriest());
        addCreatureReady(player1, new ShadowRitePriest());
        harness.setLibrary(player1, List.of(new CutDown(), new ShadowRitePriest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Shadow-Rite Priest");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Cut Down", "Shadow-Rite Priest");
        assertThat(countPermanents(player1, "Shadow-Rite Priest")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSacrificeANoncreatureClericPermanent() {
        addCreatureReady(player1, new ShadowRitePriest());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.setLibrary(player1, List.of(new ShadowRitePriest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player1, "CLERIC");
        assertThat(gqs.hasEffectiveSubtype(gd, enchantment, CardSubtype.CLERIC)).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Shadow-Rite Priest")).isEqualTo(2);
    }
}
