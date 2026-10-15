package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CauldronHaze;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
import com.github.laxika.magicalvibes.cards.s.SoulReap;
import com.github.laxika.magicalvibes.cards.s.SoulSnuffers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmbraStalker.class, SoulReap.class, SoulSnuffers.class, Snakeform.class, CauldronHaze.class})
class UmbraStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Umbra Stalker is 0/0 with an empty graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T equals the black mana symbols in one graveyard card ({1}{B} = 1)")
    void ptEqualsSingleBlackPip() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new SoulReap()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple black pips in one card count individually ({2}{B}{B} = 2)")
    void countsMultipleBlackPipsInOneCard() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new SoulSnuffers()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Black pips are summed across all graveyard cards (1 + 2 = 3)")
    void sumsAcrossGraveyardCards() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new SoulReap(), new SoulSnuffers()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-black cards contribute no black mana symbols")
    void ignoresNonBlackCards() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new Snakeform(), new Snakeform()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the controller's graveyard is counted, not the opponent's")
    void countsOnlyControllerGraveyard() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new SoulReap()));
        harness.setGraveyard(player2, List.of(new SoulSnuffers()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when a black card is added to the graveyard")
    void ptUpdatesWhenBlackCardAdded() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new SoulReap()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new SoulSnuffers());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("A hybrid black mana symbol counts as one black symbol")
    void countsBlackHybridSymbol() {
        Permanent perm = addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of(new CauldronHaze()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Umbra Stalker applies its graveyard-dependent power and toughness")
    void castingUsesGraveyardSymbols() {
        harness.setGraveyard(player1, List.of(new SoulReap(), new SoulSnuffers()));
        harness.castFromHand(player1, new UmbraStalker(), "{4}{B}{B}{B}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Umbra Stalker");
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Umbra Stalker dies when its graveyard no longer contains black mana symbols")
    void diesWhenGraveyardStopsContributingSymbols() {
        harness.setGraveyard(player1, List.of(new SoulReap()));
        addCreatureReady(player1, new UmbraStalker());
        harness.setGraveyard(player1, List.of());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Umbra Stalker");
        harness.assertInGraveyard(player1, "Umbra Stalker");
    }

    @Test
    @DisplayName("Umbra Stalker counts its own black symbols while in the graveyard")
    void countsItsOwnSymbolsInGraveyard() {
        UmbraStalker card = new UmbraStalker();
        harness.setGraveyard(player1, List.of(card, new SoulReap()));
        harness.setGraveyard(player2, List.of(new SoulSnuffers()));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(4);
    }
}
