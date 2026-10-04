package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SecludedGlen;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeblightsEnding.class, GrizzlyBears.class, LlanowarElves.class,
        AmoeboidChangeling.class, WoodlandChangeling.class, GoldmeadowHarrier.class, SecludedGlen.class})
class EyeblightsEndingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target non-Elf creature and moves it to graveyard")
    void resolvingDestroysNonElfCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an Elf creature")
    void cannotTargetElf() {
        // Add a non-Elf creature so the spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent elf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Elf creature");
    }

    @Test
    @DisplayName("Can destroy its controller's non-Elf creature")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Goldmeadow Harrier");
        harness.assertInGraveyard(player1, "Goldmeadow Harrier");
    }

    @Test
    @DisplayName("Changeling creatures are Elves and cannot be targeted")
    void cannotTargetChangeling() {
        harness.addToBattlefield(player1, new GoldmeadowHarrier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Elf creature");
    }

    @Test
    @DisplayName("A target that becomes an Elf before resolution is not destroyed")
    void targetBecomingElfIsIllegalAtResolution() {
        addCreatureReady(player2, new AmoeboidChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldmeadowHarrier());
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goldmeadow Harrier");
        harness.assertNotInGraveyard(player2, "Goldmeadow Harrier");
        harness.assertInGraveyard(player1, "Eyeblight's Ending");
    }

    @Test
    @DisplayName("A changeling that loses all creature types becomes a legal target")
    void destroysChangelingAfterItLosesCreatureTypes() {
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GoldmeadowHarrier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SecludedGlen());
        harness.setHand(player1, List.of(new EyeblightsEnding()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
