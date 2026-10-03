package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.Lhurgoyf;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoramTheUndertaker.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Lhurgoyf.class, SuntailHawk.class, TomeScour.class})
class CoramTheUndertakerTest extends BaseCardTest {

    @Test
    @DisplayName("gets +X/+0 from the greatest creature-card power in all graveyards")
    void getsPowerFromGreatestCreatureCardPowerInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new HillGiant()));

        Permanent coram = castCoram();

        assertThat(gqs.getEffectivePower(gd, coram)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, coram)).isEqualTo(5);

        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, coram)).isEqualTo(3);
    }

    @Test
    @DisplayName("mills one card for each player when it attacks")
    void millsEachPlayerWhenItAttacks() {
        Card playerOneCard = new SuntailHawk();
        Card playerTwoCard = new SuntailHawk();
        harness.setLibrary(player1, List.of(playerOneCard));
        harness.setLibrary(player2, List.of(playerTwoCard));
        Permanent coram = castCoram();
        coram.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(coram)));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(playerOneCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(playerTwoCard);
    }

    @Test
    @DisplayName("plays one milled land and casts one milled spell from any graveyard")
    void playsOneLandAndCastsOneSpellFromAnyGraveyard() {
        Card forest = new Forest();
        Card firstSpell = new GrizzlyBears();
        Card secondSpell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(
                forest, firstSpell, secondSpell, new SuntailHawk(), new SuntailHawk()));
        Card tomeScour = new TomeScour();
        harness.setHand(player1, List.of(tomeScour));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        castCoram();

        harness.playGraveyardLand(player1, forest.getId());
        harness.castFromGraveyard(player1, firstSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == firstSpell);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, secondSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, tomeScour.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("has no power bonus when graveyards contain no creature cards")
    void hasNoBonusWithoutCreatureCards() {
        Permanent coram = castCoram();
        assertThat(gqs.getEffectivePower(gd, coram)).isZero();

        harness.setGraveyard(player1, List.of(new Forest(), new TomeScour()));
        assertThat(gqs.getEffectivePower(gd, coram)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, coram)).isEqualTo(5);
    }

    @Test
    @DisplayName("updates its bonus when the greatest-power creature leaves a graveyard")
    void updatesBonusWhenGreatestCreatureLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new HillGiant()));
        Permanent coram = castCoram();
        assertThat(gqs.getEffectivePower(gd, coram)).isEqualTo(3);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, coram)).isEqualTo(2);
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, coram)).isZero();
    }

    @Test
    @DisplayName("uses characteristic-defined creature power in graveyards")
    void usesCharacteristicDefinedGraveyardPower() {
        harness.setGraveyard(player1, List.of(new Lhurgoyf(), new SuntailHawk()));
        harness.setGraveyard(player2, List.of(new SuntailHawk(), new SuntailHawk()));
        Permanent coram = castCoram();

        assertThat(gqs.getEffectivePower(gd, coram)).isEqualTo(4);
        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, coram)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, coram)).isEqualTo(5);
    }

    @Test
    @DisplayName("does not grant permission for cards already in graveyards")
    void cannotPlayCardsNotPutIntoGraveyardsFromLibrariesThisTurn() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bears));
        castCoram();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("requires mana and allows the land play after the spell cast")
    void paysManaAndUsesSpellAndLandPermissionsIndependently() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        millOwnCards(List.of(forest, bears));
        castCoram();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player1, bears.getId());
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, forest.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bears)
                .anyMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest, bears);
    }

    @Test
    @DisplayName("does not grant an additional land play")
    void cannotPlayGraveyardLandAfterPlayingLandFromHand() {
        Card forest = new Forest();
        millOwnCards(List.of(forest));
        castCoram();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("requires normal main-phase timing for lands and creature spells")
    void cannotPlayLandOrCreatureDuringCombat() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        millOwnCards(List.of(forest, bears));
        castCoram();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cannot use the permission after Coram leaves the battlefield")
    void losesPermissionWhenCoramLeaves() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        millOwnCards(List.of(forest, bears));
        Permanent coram = castCoram();
        gd.playerBattlefields.get(player1.getId()).remove(coram);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void millOwnCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
    }

    private Permanent castCoram() {
        harness.castFromHand(player1, new CoramTheUndertaker(), "{1}{B}{R}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Coram, the Undertaker");
    }
}
