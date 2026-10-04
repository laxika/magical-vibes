package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiligreeAttendant.class, DarksteelRelic.class, GrizzlyBears.class})
class FiligreeAttendantTest extends BaseCardTest {

    @Test
    void powerEqualsArtifactsItsControllerControlsAndToughnessStaysThree() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);

        harness.addToBattlefield(player1, new DarksteelRelic());
        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);
    }

    @Test
    void countsOnlyArtifactsControlledByItsController() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DarksteelRelic());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);
    }

    @Test
    void characteristicPowerWorksInHandAndGraveyardWithoutCountingThoseCards() {
        FiligreeAttendant inHand = new FiligreeAttendant();
        FiligreeAttendant inGraveyard = new FiligreeAttendant();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player2, new FiligreeAttendant());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(3);

        harness.addToBattlefield(player1, new FiligreeAttendant());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
    }

    @Test
    void powerUpdatesWhenAnotherArtifactLeavesTheBattlefield() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(otherArtifact);
        harness.setGraveyard(player1, List.of(otherArtifact.getCard()));

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);
    }

    @Test
    void powerUsesCurrentControllerRatherThanOwner() {
        FiligreeAttendant card = new FiligreeAttendant();
        card.setOwnerId(player1.getId());
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, card);
        harness.addToBattlefield(player1, new FiligreeAttendant());
        harness.addToBattlefield(player2, new FiligreeAttendant());
        harness.addToBattlefield(player2, new FiligreeAttendant());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(attendant);
        gd.playerBattlefields.get(player2.getId()).add(attendant);

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);
    }
}
