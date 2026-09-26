package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Absorb;
import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.u.UrborgSkeleton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Obliterate.class, Forest.class, GrizzlyBears.class, GloriousAnthem.class, Millstone.class, ManaLeak.class, Absorb.class, AncientKavu.class, ChromaticSphere.class, DuelingGrounds.class, UrborgSkeleton.class})
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

    @Test
    @DisplayName("Cannot be countered by Absorb")
    void cannotBeCounteredByAbsorb() {
        harness.addToBattlefield(player2, new AncientKavu());
        Obliterate obliterate = new Obliterate();
        harness.castFromHand(player1, obliterate, "{6}{R}{R}");

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, obliterate.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ancient Kavu");
        harness.assertInGraveyard(player2, "Absorb");
    }

    @Test
    @DisplayName("Permanents cannot regenerate from Obliterate")
    void permanentsCannotRegenerate() {
        UrborgSkeleton skeleton = new UrborgSkeleton();
        harness.addToBattlefield(player2, skeleton);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Urborg Skeleton").getRegenerationShield()).isEqualTo(1);

        harness.castFromHand(player1, new Obliterate(), "{6}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urborg Skeleton");
        harness.assertInGraveyard(player2, "Urborg Skeleton");
    }
}
