package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoramTheUndertaker.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        SuntailHawk.class, TomeScour.class})
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

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

    private Permanent castCoram() {
        harness.setHand(player1, List.of(new CoramTheUndertaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Coram, the Undertaker");
    }
}
