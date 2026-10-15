package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.s.SigiledSkink;
import com.github.laxika.magicalvibes.cards.f.FleetfeatherCockatrice;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallOfTriumph.class, GoldenHind.class, SigiledSkink.class, FleetfeatherCockatrice.class})
class HallOfTriumphTest extends BaseCardTest {

    @Test
    void resolvingAwaitsColorChoice() {
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hall of Triumph");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorBoostsOnlyMatchingCreaturesYouControl() {
        harness.addToBattlefield(player1, new GoldenHind());
        harness.addToBattlefield(player1, new SigiledSkink());
        harness.addToBattlefield(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent ownGreen = findPermanent(player1, "Golden Hind");
        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(1);

        harness.handleListChoice(player1, "GREEN");

        Permanent ownRed = findPermanent(player1, "Sigiled Skink");
        Permanent opponentGreen = findPermanent(player2, "Golden Hind");
        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownRed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownRed)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGreen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGreen)).isEqualTo(1);
    }

    @Test
    void multicoloredCreatureGetsOneBonusForMatchingChosenColor() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FleetfeatherCockatrice());
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void matchingCreatureEnteringLaterGetsBonus() {
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GoldenHind());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @CardUsed({EnsoulArtifact.class, PaintersServant.class})
    void animatedHallOfChosenColorBoostsItself() {
        harness.setHand(player1, List.of(new HallOfTriumph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player1, List.of(new PaintersServant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent hall = findPermanent(player1, "Hall of Triumph");
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, hall.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hall)).isEqualTo(6);
    }
}
