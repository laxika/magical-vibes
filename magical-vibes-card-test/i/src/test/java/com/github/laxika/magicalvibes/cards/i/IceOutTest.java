package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceOut.class, DarksteelRelic.class, GrizzlyBears.class, HopefulVigil.class})
class IceOutTest extends BaseCardTest {

    @Test
    void countersSpellAtFullCost() {
        GrizzlyBears bears = castCreatureSpell();
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bargainCountersSpellForTwoManaAndSacrificesArtifact() {
        GrizzlyBears bears = castCreatureSpell();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedInstantWithSacrifice(player2, 0, bears.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Darksteel Relic");
    }

    @Test
    void cannotBargainBySacrificingCreature() {
        GrizzlyBears bears = castCreatureSpell();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player2, 0, bears.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    @Test
    void cannotPayReducedCostWithoutBargaining() {
        GrizzlyBears bears = castCreatureSpell();
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void bargainDoesNotReduceColoredManaRequirements() {
        GrizzlyBears bears = castCreatureSpell();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player2, 0, bears.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotBargainWithOpponentsArtifact() {
        GrizzlyBears bears = castCreatureSpell();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player2, 0, bears.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");

        harness.assertOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    void bargainCanSacrificeEnchantmentAndPaysCostBeforeResolution() {
        GrizzlyBears bears = castCreatureSpell();
        harness.setLibrary(player2, List.of());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new HopefulVigil());
        harness.setHand(player2, List.of(new IceOut()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedInstantWithSacrifice(player2, 0, bears.getId(), sacrifice.getId());

        harness.assertInGraveyard(player2, "Hopeful Vigil");
        harness.assertNotOnBattlefield(player2, "Hopeful Vigil");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bargainCanSacrificeNonartifactNonenchantmentCreatureToken() {
        harness.castFromHand(player1, new HopefulVigil(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent knight = findPermanents(player1, "Knight").getFirst();
        GrizzlyBears bears = castCreatureSpell();
        harness.setHand(player1, List.of(new IceOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, bears.getId(), knight.getId());

        harness.assertNotOnBattlefield(player1, "Knight");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hopeful Vigil");
    }

    private GrizzlyBears castCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{G}{G}");
        harness.passPriority(player1);
        return bears;
    }
}
