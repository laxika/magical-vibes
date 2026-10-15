package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwiftKick.class, GrizzlyBears.class, LlanowarElves.class, WetlandSambar.class, SummitProwler.class})
class SwiftKickTest extends BaseCardTest {

    @Test
    @DisplayName("The boost applies before the fight")
    void boostAppliesBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, elvesId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+0 wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, elvesId));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boosted power determines lethal fight damage in both directions")
    void boostedFightDealsLethalDamageInBothDirections() {
        Permanent sambar = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, List.of(sambar.getId(), prowler.getId()));

        harness.assertInGraveyard(player1, "Wetland Sambar");
        harness.assertInGraveyard(player2, "Summit Prowler");
        harness.assertInGraveyard(player1, "Swift Kick");
    }

    @Test
    @DisplayName("A missing opposing target does not stop the boost")
    void opposingTargetLeavesBeforeResolution() {
        Permanent sambar = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, List.of(sambar.getId(), prowler.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(prowler);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wetland Sambar");
        assertThat(gqs.getEffectivePower(gd, sambar)).isEqualTo(3);
        assertThat(sambar.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Swift Kick");
    }

    @Test
    @DisplayName("A missing friendly target leaves the opposing creature unchanged")
    void friendlyTargetLeavesBeforeResolution() {
        Permanent sambar = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new SummitProwler());
        harness.setHand(player1, List.of(new SwiftKick()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, List.of(sambar.getId(), prowler.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(sambar);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Summit Prowler");
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(prowler.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Swift Kick");
    }
}
