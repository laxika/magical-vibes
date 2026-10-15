package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HideousEnd.class, BottleGnomes.class, GrizzlyBears.class, MassOfGhouls.class,
        RiverBoa.class, IntoTheRoil.class})
class HideousEndTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and its controller loses 2 life")
    void destroysNonblackCreatureAndLosesLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new HideousEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target an artifact creature if it is nonblack")
    void canTargetNonblackArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());

        harness.setHand(player1, List.of(new HideousEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, artifactCreature.getId());

        harness.assertInGraveyard(player2, "Bottle Gnomes");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());

        harness.setHand(player1, List.of(new HideousEnd()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    void ownCreatureControllerLosesLife() {
        Permanent boa = harness.addToBattlefieldAndReturn(player1, new RiverBoa());
        harness.setHand(player1, List.of(new HideousEnd()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, boa.getId());

        harness.assertInGraveyard(player1, "River Boa");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void regeneratedCreatureSurvivesButControllerStillLosesLife() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        harness.setHand(player1, List.of(new HideousEnd()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, boa.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "River Boa");
        harness.assertNotInGraveyard(player2, "River Boa");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void missingTargetPreventsLifeLoss() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        harness.setHand(player1, List.of(new HideousEnd()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, boa.getId());
        harness.castAndResolveInstant(player2, 0, boa.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "River Boa");
        harness.assertInGraveyard(player1, "Hideous End");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
