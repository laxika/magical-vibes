package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarkOfAsylum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicFallout.class, GrizzlyBears.class, GiantSpider.class, Cancel.class, MarkOfAsylum.class, VedalkenOutlander.class})
class VolcanicFalloutTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature and each player")
    void dealsTwoToEachCreatureAndPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);

        // Both 2/2 creatures die to 2 damage.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Both players take 2 damage.
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Creatures with toughness greater than 2 survive")
    void toughCreaturesSurvive() {
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Can't be countered — Cancel resolves but damage is still dealt")
    void cannotBeCountered() {
        VolcanicFallout fallout = new VolcanicFallout();
        harness.setHand(player1, List.of(fallout));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fallout.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Cancel resolved but couldn't counter — Volcanic Fallout still dealt 2 to each player.
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Cancel");
    }
    @Test
    @DisplayName("Damage to surviving creatures accumulates across two resolutions")
    void damageAccumulatesOnSurvivingCreatures() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new VolcanicFallout(), new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0);
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Noncombat damage prevention protects only its controller's creatures")
    void damageCanBePreventedDespiteBeingUncounterable() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Mark of Asylum");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Protection from red prevents damage to artifact creatures")
    void protectionFromRedPreventsDamage() {
        harness.addToBattlefield(player2, new VedalkenOutlander());
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Vedalken Outlander");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
