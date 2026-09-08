package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeterParker.class, GrizzlyBears.class})
class PeterParkerTest extends BaseCardTest {

    @Test
    @DisplayName("Peter Parker creates a Spider token when entering the battlefield")
    void enteringCreatesSpiderToken() {
        harness.setHand(player1, List.of(new PeterParker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent spider = findPermanent(player1, "Spider");
        assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Peter Parker can transform into Amazing Spider-Man at sorcery speed")
    void transformsIntoAmazingSpiderMan() {
        addCreatureReady(player1, new PeterParker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amazing Spider-Man");
    }

    @Test
    @DisplayName("Peter Parker can be cast as Amazing Spider-Man")
    void castsBackFace() {
        harness.setHand(player1, List.of(new PeterParker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amazing Spider-Man");
    }

    @Test
    @DisplayName("Amazing Spider-Man lets a legendary colored spell use web-slinging")
    void castsWithWebSlinging() {
        PeterParker source = new PeterParker();
        harness.addToBattlefieldAndReturn(player1, source.getBackFaceCard());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new PeterParker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Peter Parker");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Web-slinging requires a tapped creature")
    void webSlingingRequiresTappedCreature() {
        PeterParker source = new PeterParker();
        harness.addToBattlefieldAndReturn(player1, source.getBackFaceCard());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PeterParker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(untappedCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }
}
