package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Obliterate.class, Forest.class, GrizzlyBears.class, GloriousAnthem.class,
        Millstone.class, ManaLeak.class})
class ObliterateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts, creatures, and lands")
    void destroysArtifactsCreaturesAndLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Obliterate(), "{6}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Leaves enchantments untouched")
    void leavesEnchantmentsUntouched() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Obliterate(), "{6}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);

        Obliterate obliterate = new Obliterate();
        harness.castFromHand(player1, obliterate, "{6}{R}{R}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, obliterate.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player1, "Obliterate");
        harness.assertInGraveyard(player2, "Mana Leak");
    }

    @Test
    @DisplayName("Destroyed creatures cannot be regenerated")
    void destroyedCreaturesCannotBeRegenerated() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.castFromHand(player1, new Obliterate(), "{6}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Obliterate(), "{6}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Obliterate");
    }
}
