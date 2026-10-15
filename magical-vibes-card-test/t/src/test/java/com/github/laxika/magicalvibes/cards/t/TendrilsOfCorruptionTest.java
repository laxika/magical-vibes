package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TendrilsOfCorruption.class, RuneclawBear.class, Plains.class, Swamp.class, SafePassage.class})
class TendrilsOfCorruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tendrils of Corruption targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Tendrils deals damage to creature equal to Swamps controlled and gains life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, targetId);

        // 3 damage kills Runeclaw Bear (2 toughness)
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        // Controller gains 3 life (equal to Swamp count)
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Tendrils counts only controller's Swamps, not opponent's")
    void countsOnlyControllerSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Only 1 damage (1 Swamp controlled by player1), Runeclaw Bear survives (2 toughness)
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        // Controller gains 1 life
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tendrils counts Swamps at resolution time")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, targetId);

        // Remove all Swamps before resolution
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Swamp"));

        harness.passBothPriorities();

        // 0 Swamps at resolution, so 0 damage and 0 life gain
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tendrils does not count non-Swamp lands")
    void doesNotCountNonSwampLands() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Only 1 Swamp, so 1 damage (Runeclaw Bear survives) and 1 life gained
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tendrils fizzles when its target creature leaves before resolution")
    void fizzlesWhenTargetCreatureLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RuneclawBear()).getId();
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(permanent -> permanent.getId().equals(targetId));

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage prevention does not prevent Tendrils' life gain")
    void gainsLifeWhenDamageIsPrevented() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        var target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Tendrils of Corruption");
    }

    @Test
    @DisplayName("Tendrils can damage its controller's creature and still gain life")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Swamp());
        var target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Swamps added after casting increase both damage and life gain")
    void countsSwampsAddedBeforeResolution() {
        harness.addToBattlefield(player1, new Swamp());
        var target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tendrils cannot target a noncreature land")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        var land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new TendrilsOfCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
