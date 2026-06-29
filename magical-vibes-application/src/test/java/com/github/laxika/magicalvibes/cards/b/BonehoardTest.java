package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BonehoardTest extends BaseCardTest {

    // ===== Living weapon ETB =====

    @Test
    @DisplayName("Casting Bonehoard triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Bonehoard");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Need at least 1 creature in graveyard so Germ survives (0/0 + X/X where X >= 1)
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve living weapon ETB

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        Permanent bonehoard = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Bonehoard"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(bonehoard.getAttachedTo()).isEqualTo(germ.getId());
    }

    // ===== +X/+X where X is creature cards in all graveyards =====

    @Test
    @DisplayName("Germ dies to SBAs when no creature cards in any graveyard (0/0 toughness)")
    void germDiesWithEmptyGraveyards() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve living weapon ETB — Germ is 0/0, dies to SBA

        // Germ should be dead (0/0 toughness with no creatures in graveyards)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));

        // Bonehoard should remain on the battlefield unattached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonehoard"));
    }

    @Test
    @DisplayName("Germ gets +X/+X from creature cards in controller's graveyard")
    void germGetsBoostFromControllerGraveyard() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put 2 creature cards in player1's graveyard
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // 0/0 base + 2/2 boost = 2/2
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts creature cards in all players' graveyards")
    void boostCountsAllGraveyards() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put 1 creature in player1's graveyard, 2 in player2's graveyard
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // 0/0 base + 3/3 boost (1 + 2 creature cards) = 3/3
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost updates dynamically when creatures enter graveyards")
    void boostUpdatesDynamically() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Start with 1 creature so Germ survives
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // Initially 1/1 (one creature in graveyard)
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);

        // Add another creature to graveyard — now 2/2
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);

        // Add a third — now 3/3
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    // ===== Equip to another creature =====

    @Test
    @DisplayName("Equipping Bonehoard to another creature applies the graveyard-based boost")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put 2 creatures in graveyards
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent bonehoard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bonehoard"))
                .findFirst().orElseThrow();

        assertThat(bonehoard.getAttachedTo()).isEqualTo(bears.getId());

        // Bears: 2/2 base + 2/2 boost (tokens don't count as creature cards) = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Germ dies when Bonehoard is moved to another creature (0/0 with no equipment)")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Bonehoard()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Put a creature in graveyard so germ survives initially
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Equip to bears
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness after losing equipment boost, SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));
    }
}
