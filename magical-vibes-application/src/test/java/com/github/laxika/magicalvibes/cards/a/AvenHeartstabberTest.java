package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenHeartstabber.class, Forest.class, GrizzlyBears.class, HillGiant.class, Island.class,
        Mountain.class, Murder.class, Shock.class})
class AvenHeartstabberTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 and deathtouch with five distinct graveyard mana values")
    void gainsBoostAndDeathtouchAtThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Murder(), new HillGiant()));
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenHeartstabber());

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aven, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not get the graveyard bonus with fewer than five distinct mana values")
    void doesNotGainBonusBelowThreshold() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Murder(), new Shock()));
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenHeartstabber());

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, aven, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("When it dies, mills two cards then draws a card")
    void deathTriggerMillsThenDraws() {
        Permanent aven = harness.addToBattlefieldAndReturn(player1, new AvenHeartstabber());
        Card milledFirst = new Island();
        Card milledSecond = new Mountain();
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(milledFirst, milledSecond, drawn));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, aven.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledFirst, milledSecond);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }
}
