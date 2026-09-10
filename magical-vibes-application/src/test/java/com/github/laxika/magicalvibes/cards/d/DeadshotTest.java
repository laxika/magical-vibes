package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deadshot.class, Forest.class, HornedTurtle.class, MoggConscripts.class, MoggFanatic.class,
        TrainedArmodon.class})
class DeadshotTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the first target and it deals damage equal to its power to the second target")
    void tapsFirstTargetAndDealsPowerDamage() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        harness.addToBattlefieldAndReturn(player2, new MoggFanatic());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(conscripts.getId(), harness.getPermanentId(player2, "Mogg Fanatic")));

        assertThat(conscripts.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Target survives when the tapped creature's power is below its toughness")
    void targetSurvivesInsufficientDamage() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(conscripts.getId(), harness.getPermanentId(player2, "Horned Turtle")));

        assertThat(conscripts.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Horned Turtle");
    }

    @Test
    @DisplayName("Both targets may be creatures the caster controls")
    void mayTapOwnCreatureToShootOwnCreature() {
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.addToBattlefieldAndReturn(player1, new MoggFanatic());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0,
                List.of(armodon.getId(), harness.getPermanentId(player1, "Mogg Fanatic")));

        assertThat(armodon.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotChooseSameCreatureTwice() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(conscripts.getId(), conscripts.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both targets must be creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(conscripts.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already-tapped first target still deals damage")
    void alreadyTappedFirstTargetStillDealsDamage() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        conscripts.tap();
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(conscripts.getId(), turtle.getId()));

        assertThat(conscripts.isTapped()).isTrue();
        assertThat(turtle.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Uses the first target's power when the spell resolves")
    void usesPowerAtResolution() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, List.of(conscripts.getId(), turtle.getId()));
        conscripts.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(turtle.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("No damage is dealt when the tapped creature leaves before resolution")
    void noDamageWhenShooterRemoved() {
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new MoggConscripts());
        harness.addToBattlefieldAndReturn(player2, new MoggFanatic());
        harness.setHand(player1, List.of(new Deadshot()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0,
                List.of(conscripts.getId(), harness.getPermanentId(player2, "Mogg Fanatic")));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mogg Fanatic");
    }
}
