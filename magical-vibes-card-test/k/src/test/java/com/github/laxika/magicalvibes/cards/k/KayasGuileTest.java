package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HazoretsUndyingFury;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayasGuile.class, FugitiveWizard.class, GrizzlyBears.class, HazoretsUndyingFury.class, PastInFlames.class})
class KayasGuileTest extends BaseCardTest {

    @Test
    void sacrificesAnOpposingCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLife(player1, 20);

        cast(new int[]{0, 3}, 1);

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void exilesOpponentsGraveyardsAndCreatesFlyingSpirit() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));

        cast(new int[]{1, 2}, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().hasKeyword(Keyword.FLYING));
    }

    @Test
    void entwinePaysOneFixedCostAndResolvesAllModes() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLife(player1, 20);

        cast(new int[]{0, 1, 2, 3}, 4);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(graveyardCard, opponentCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().hasKeyword(Keyword.FLYING));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void entwineRejectsSelectingThreeModes() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new KayasGuile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, 4, new int[]{0, 1, 2}, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2 modes");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
    }

    @Test
    void createsExactlyOneWhiteAndBlackSpiritAndGainsLifeWithoutAffectingOpponent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLife(player1, 10);

        cast(new int[]{2, 3}, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Card spirit = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        assertThat(spirit.isToken()).isTrue();
        assertThat(spirit.getPower()).isEqualTo(1);
        assertThat(spirit.getToughness()).isEqualTo(1);
        assertThat(spirit.getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertLife(player1, 14);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    void emptyOpposingZonesDoNotPreventEntwinedSpellFromResolving() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Card ownGraveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setLife(player1, 20);

        cast(new int[]{0, 1, 2, 3}, 4);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player1, 24);
    }

    @Test
    void opponentChoosesSacrificeBeforeLaterEntwinedModesResolve() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        cast(new int[]{0, 1, 2, 3}, 4);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(wizard, bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(wizard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears.getCard());
        harness.assertOnBattlefield(player1, "Spirit");
        harness.assertLife(player1, 24);
    }

    @Test
    void cannotChooseAllModesWithoutEnoughManaForEntwine() {
        harness.setHand(player1, List.of(new KayasGuile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, 4, new int[]{0, 1, 2, 3}, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Kaya's Guile");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cast for free from exile, it may pay entwine to choose all four modes, with no stop at three")
    void freeCastMayPayEntwineForAllModes() {
        KayasGuile guile = new KayasGuile();
        harness.setLife(player1, 20);
        castHazoretRevealing(guile);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.handleMultipleCardsChosen(player1, List.of(guile.getId()));
        harness.handleListChoice(player1, "You gain 4 life");
        harness.handleListChoice(player1, "Create a 1/1 white and black Spirit creature token with flying");
        assertThat(modeOptions()).contains(ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handleListChoice(player1, "Exile all opponents' graveyards");
        assertThat(modeOptions()).containsExactly("Each opponent sacrifices a creature of their choice");
        harness.handleListChoice(player1, "Each opponent sacrifices a creature of their choice");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Cast for free from exile, it may stop at two modes without paying entwine")
    void freeCastStopsAtTwoModesWithoutEntwine() {
        KayasGuile guile = new KayasGuile();
        harness.setLife(player1, 20);
        castHazoretRevealing(guile);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.handleMultipleCardsChosen(player1, List.of(guile.getId()));
        harness.handleListChoice(player1, "You gain 4 life");
        harness.handleListChoice(player1, "Create a 1/1 white and black Spirit creature token with flying");
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Cast for free from exile without mana for entwine, it is cast with the two modes chosen")
    void freeCastWithoutEntwineManaCastsTwoModes() {
        KayasGuile guile = new KayasGuile();
        harness.setLife(player1, 20);
        castHazoretRevealing(guile);
        gd.playerManaPools.get(player1.getId()).clear();

        harness.handleMultipleCardsChosen(player1, List.of(guile.getId()));
        harness.handleListChoice(player1, "You gain 4 life");
        harness.handleListChoice(player1, "Create a 1/1 white and black Spirit creature token with flying");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Kaya's Guile"));
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Flashback with all four modes pays entwine with the flashback cost")
    void flashbackPaysEntwineForAllModes() {
        KayasGuile guile = grantFlashbackToGuile();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, graveyardIndex(guile),
                ChooseOneEffect.encodeModeSelection(2, 4, new int[]{0, 1, 2, 3}), null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Flashback with all four modes is refused, paying nothing, when the entwine mana is missing")
    void flashbackRefusesAllModesWithoutEntwineMana() {
        KayasGuile guile = grantFlashbackToGuile();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, graveyardIndex(guile),
                ChooseOneEffect.encodeModeSelection(2, 4, new int[]{0, 1, 2, 3}), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(guile);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback with the two modes specified is cast without entwine")
    void flashbackCastsTwoModes() {
        KayasGuile guile = grantFlashbackToGuile();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, graveyardIndex(guile),
                ChooseOneEffect.encodeModeSelection(2, 4, new int[]{2, 3}), null);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Spirit");
    }

    private List<String> modeOptions() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        return ((PendingInteraction.ColorChoice) gd.interaction.activeInteraction()).options();
    }

    private void castHazoretRevealing(Card card) {
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new HazoretsUndyingFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private KayasGuile grantFlashbackToGuile() {
        KayasGuile guile = new KayasGuile();
        harness.setGraveyard(player1, List.of(guile));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        return guile;
    }

    private int graveyardIndex(Card card) {
        return gd.playerGraveyards.get(player1.getId()).indexOf(card);
    }

    private void cast(int[] modes, int colorlessMana) {
        harness.setHand(player1, List.of(new KayasGuile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 2, 4, modes, List.of());
        harness.passBothPriorities();
    }
}
