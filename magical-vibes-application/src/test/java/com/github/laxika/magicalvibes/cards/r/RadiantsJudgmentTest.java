package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.s.SickAndTired;
import com.github.laxika.magicalvibes.cards.y.YavimayaGranger;
import com.github.laxika.magicalvibes.cards.y.YavimayaScion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadiantsJudgment.class, YavimayaScion.class, YavimayaGranger.class,
        GrimMonolith.class, SickAndTired.class})
class RadiantsJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with power 4 or greater")
    void destroysHighPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaScion());

        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Yavimaya Scion");
        harness.assertInGraveyard(player2, "Yavimaya Scion");
        harness.assertInGraveyard(player1, "Radiant's Judgment");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaGranger());

        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());

        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles when the target's power falls below 4 before resolution")
    void fizzlesWhenTargetLosesPowerBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaScion());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new YavimayaGranger());

        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new SickAndTired()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, List.of(target.getId(), otherCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Yavimaya Scion");
        harness.assertNotInGraveyard(player2, "Yavimaya Scion");
        harness.assertInGraveyard(player1, "Radiant's Judgment");
    }

    @Test
    @DisplayName("Allows the target to regenerate")
    void allowsRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaScion());
        target.setRegenerationShield(1);

        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Yavimaya Scion");
        harness.assertNotInGraveyard(player2, "Yavimaya Scion");
        harness.assertInGraveyard(player1, "Radiant's Judgment");
    }

    @Test
    @DisplayName("Cycling discards Radiant's Judgment and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.setLibrary(player1, List.of(new YavimayaGranger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Radiant's Judgment");
        harness.assertInHand(player1, "Yavimaya Granger");
    }
}
