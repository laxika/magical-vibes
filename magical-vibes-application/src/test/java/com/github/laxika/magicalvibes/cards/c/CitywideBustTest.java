package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CitywideBust.class, GrizzlyBears.class, PithingNeedle.class, SerraAngel.class})
class CitywideBustTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every creature with toughness 4 or greater on both battlefields")
    void destroysCreaturesWithToughnessAtLeastFour() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.setHand(player1, List.of(new CitywideBust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Pithing Needle");
    }

    @Test
    @DisplayName("Uses toughness at resolution, including increases and decreases")
    void usesEffectiveToughnessAtResolution() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new CitywideBust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        bears.setToughnessModifier(3);
        angel.setToughnessModifier(-1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertNotInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Marked damage does not reduce toughness for the destruction threshold")
    void destroysDamagedCreatureWithFourToughness() {
        var angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setMarkedDamage(3);
        harness.setHand(player1, List.of(new CitywideBust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Resolves without any qualifying creatures")
    void resolvesWithoutQualifyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CitywideBust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Citywide Bust");
    }
}
