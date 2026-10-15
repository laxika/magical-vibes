package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WardscaleDragon;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.cards.h.HerosBlade;
import com.github.laxika.magicalvibes.cards.v.ValorousStance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CruxOfFate.class, WardscaleDragon.class, TyphoidRats.class,
        HerosBlade.class, ValorousStance.class})
class CruxOfFateTest extends BaseCardTest {

    private void castCruxOfFate(int mode) {
        harness.setHand(player1, List.of(new CruxOfFate()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, mode);
    }

    @Test
    @DisplayName("Destroys all Dragon creatures and spares non-Dragon creatures")
    void destroysDragons() {
        harness.addToBattlefield(player1, new WardscaleDragon());
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new WardscaleDragon());
        harness.addToBattlefield(player2, new TyphoidRats());

        castCruxOfFate(0);

        harness.assertNotOnBattlefield(player1, "Wardscale Dragon");
        harness.assertNotOnBattlefield(player2, "Wardscale Dragon");
        harness.assertOnBattlefield(player1, "Typhoid Rats");
        harness.assertOnBattlefield(player2, "Typhoid Rats");
    }

    @Test
    @DisplayName("Destroys all non-Dragon creatures and spares Dragons")
    void destroysNonDragons() {
        harness.addToBattlefield(player1, new WardscaleDragon());
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new WardscaleDragon());
        harness.addToBattlefield(player2, new TyphoidRats());

        castCruxOfFate(1);

        harness.assertOnBattlefield(player1, "Wardscale Dragon");
        harness.assertOnBattlefield(player2, "Wardscale Dragon");
        harness.assertNotOnBattlefield(player1, "Typhoid Rats");
        harness.assertNotOnBattlefield(player2, "Typhoid Rats");
    }

    @Test
    void dragonModeRespectsIndestructible() {
        harness.addToBattlefield(player1, new WardscaleDragon());
        harness.addToBattlefield(player2, new WardscaleDragon());
        protectCreature("Wardscale Dragon");

        castCruxOfFate(0);

        harness.assertOnBattlefield(player1, "Wardscale Dragon");
        harness.assertNotInGraveyard(player1, "Wardscale Dragon");
        harness.assertNotOnBattlefield(player2, "Wardscale Dragon");
        harness.assertInGraveyard(player2, "Wardscale Dragon");
        harness.assertInGraveyard(player1, "Crux of Fate");
    }

    @Test
    void nonDragonModeRespectsIndestructibleAndSparesNoncreatures() {
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new TyphoidRats());
        harness.addToBattlefield(player1, new HerosBlade());
        harness.addToBattlefield(player2, new HerosBlade());
        protectCreature("Typhoid Rats");

        castCruxOfFate(1);

        harness.assertOnBattlefield(player1, "Typhoid Rats");
        harness.assertNotInGraveyard(player1, "Typhoid Rats");
        harness.assertNotOnBattlefield(player2, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Typhoid Rats");
        harness.assertOnBattlefield(player1, "Hero's Blade");
        harness.assertOnBattlefield(player2, "Hero's Blade");
        harness.assertInGraveyard(player1, "Crux of Fate");
    }

    @Test
    void dragonModeResolvesWithoutDragons() {
        harness.addToBattlefield(player2, new TyphoidRats());
        harness.addToBattlefield(player1, new HerosBlade());

        castCruxOfFate(0);

        harness.assertOnBattlefield(player2, "Typhoid Rats");
        harness.assertOnBattlefield(player1, "Hero's Blade");
        harness.assertInGraveyard(player1, "Crux of Fate");
    }

    @Test
    void nonDragonModeResolvesWithoutNonDragonCreatures() {
        harness.addToBattlefield(player2, new WardscaleDragon());
        harness.addToBattlefield(player1, new HerosBlade());

        castCruxOfFate(1);

        harness.assertOnBattlefield(player2, "Wardscale Dragon");
        harness.assertOnBattlefield(player1, "Hero's Blade");
        harness.assertInGraveyard(player1, "Crux of Fate");
    }

    private void protectCreature(String name) {
        harness.setHand(player1, List.of(new ValorousStance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, name));
        harness.passBothPriorities();
    }
}
