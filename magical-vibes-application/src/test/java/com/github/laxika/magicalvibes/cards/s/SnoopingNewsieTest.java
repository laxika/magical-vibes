package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnoopingNewsie.class, Forest.class, GrizzlyBears.class, HillGiant.class, Island.class,
        Mountain.class, Murder.class, Shock.class})
class SnoopingNewsieTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and lifelink with five distinct graveyard mana values")
    void gainsBoostAndLifelinkAtThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Murder(), new HillGiant()));
        Permanent newsie = harness.addToBattlefieldAndReturn(player1, new SnoopingNewsie());

        assertThat(gqs.getEffectivePower(gd, newsie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newsie)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, newsie, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not get the graveyard bonus with fewer than five distinct mana values")
    void doesNotGainBonusBelowThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Murder(), new Shock()));
        Permanent newsie = harness.addToBattlefieldAndReturn(player1, new SnoopingNewsie());

        assertThat(gqs.getEffectivePower(gd, newsie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newsie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, newsie, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Enters by milling two cards")
    void entersByMillingTwoCards() {
        Card topCard = new Island();
        Card secondCard = new Mountain();
        Card remainingCard = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                topCard, secondCard, remainingCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SnoopingNewsie()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(
                topCard, secondCard);
    }
}
