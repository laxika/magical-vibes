package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SludgeCrawler;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoilingEarth.class, Forest.class, SludgeCrawler.class, OranRiefInvoker.class, ScourFromExistence.class})
class BoilingEarthTest extends BaseCardTest {

    @Test
    void dealsOneDamageToOpponentsCreaturesOnly() {
        harness.addToBattlefield(player1, new SludgeCrawler());
        harness.addToBattlefield(player2, new SludgeCrawler());
        harness.addToBattlefield(player2, new OranRiefInvoker());
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertOnBattlefield(player1, "Sludge Crawler");
        harness.assertNotOnBattlefield(player2, "Sludge Crawler");
        harness.assertOnBattlefield(player2, "Oran-Rief Invoker");
    }

    @Test
    void alternateCastAwakensTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new SludgeCrawler());
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castWithAlternateCost(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sludge Crawler");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void alternateCastRequiresLandYouControlTarget() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void normalCastDoesNotAnimateYourLandOrDamagePlayers() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Boiling Earth");
    }

    @Test
    void awakenRequiresATargetEvenWhenYouControlALand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void awakenCannotBePaidWithOnlyTheNormalManaCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalAwakenTargetPreventsAllDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new SludgeCrawler());
        harness.setHand(player1, List.of(new BoilingEarth()));
        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.castWithAlternateCost(player1, 0, land.getId());
        harness.castInstant(player2, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Sludge Crawler");
        harness.assertInGraveyard(player1, "Boiling Earth");
    }

    @Test
    void awakenPersistsAcrossTurnsAndCanAwakenTheSameLandAgain() {
        harness.setHand(player2, List.of());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BoilingEarth(), new BoilingEarth()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castWithAlternateCost(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castWithAlternateCost(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
    }
}
