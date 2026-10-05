package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayasGuile.class, FugitiveWizard.class, GrizzlyBears.class})
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

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

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

    private void cast(int[] modes, int colorlessMana) {
        harness.setHand(player1, List.of(new KayasGuile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 2, 4, modes, List.of());
        harness.passBothPriorities();
    }
}
