package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Detonate.class, RodOfRuin.class, DarksteelPlate.class, GrizzlyBears.class,
        Ornithopter.class, PlatinumEmperion.class, Shatter.class})
class DetonateTest extends BaseCardTest {

    @Test
    @CardUsed(Ornithopter.class)
    void destroysZeroManaValueArtifactWithZeroX() {
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        UUID target = targetPermanent.getId();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0, target);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Destroys target artifact with mana value X and deals X damage to its controller")
    void destroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 5); // {X=4}{R}
        harness.castAndResolveSorcery(player1, 0, 4, target);

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player2, lifeBefore - 4);
    }

    @Test
    @DisplayName("Deals damage even when the artifact cannot be destroyed")
    void dealsDamageWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate()); // indestructible, mana value 3
        UUID target = harness.getPermanentId(player2, "Darksteel Plate");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 4); // {X=3}{R}
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertOnBattlefield(player2, "Darksteel Plate");
        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    void cannotBeRegenerated() {
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        targetPermanent.setRegenerationShield(1);
        UUID target = targetPermanent.getId();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 4, target);
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player2, lifeBefore - 4);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    void destroysArtifactBeforeDealingDamageToItsController() {
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new PlatinumEmperion());
        UUID target = targetPermanent.getId();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.castAndResolveSorcery(player1, 0, 8, target);
        harness.assertNotOnBattlefield(player2, "Platinum Emperion");
        harness.assertLife(player2, lifeBefore - 8);
    }

    @Test
    @DisplayName("Cannot target an artifact whose mana value does not equal X")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 4); // {X=3}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // mana value 2, not an artifact
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 3); // {X=2}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyOwnArtifactAndDamageItsController() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new RodOfRuin()).getId();
        int casterLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4, target);

        harness.assertNotOnBattlefield(player1, "Rod of Ruin");
        harness.assertInGraveyard(player1, "Rod of Ruin");
        harness.assertLife(player1, casterLife - 4);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @CardUsed(Shatter.class)
    void dealsNoDamageWhenTargetLeavesBeforeResolution() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin()).getId();
        int casterLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new Detonate()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 4, target);
        harness.castAndResolveInstant(player2, 0, target);
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Detonate");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player1, casterLife);
        harness.assertLife(player2, opponentLife);
    }
}
