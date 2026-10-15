package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lashwrithe.class, GrizzlyBears.class, Swamp.class})
class LashwritheTest extends BaseCardTest {

    // ===== Living weapon ETB =====

    @Test
    @DisplayName("Casting Lashwrithe triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Lashwrithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lashwrithe");
    }

    @Test
    @DisplayName("Resolving living weapon creates Phyrexian Germ token and attaches Lashwrithe")
    void livingWeaponCreatesGermAndAttaches() {
        // Need at least 1 swamp so the 0/0 Germ doesn't die to state-based actions
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Lashwrithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve living weapon ETB

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        Permanent lashwrithe = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Lashwrithe"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(lashwrithe.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(germ.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(germ.getCard().getPower()).isEqualTo(0);
        assertThat(germ.getCard().getToughness()).isEqualTo(0);
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(germ.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GERM);

        // Germ is 1/1 with 1 swamp
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Germ dies to state-based actions with no Swamps (0/0 toughness)")
    void germDiesWithNoSwamps() {
        harness.setHand(player1, List.of(new Lashwrithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve living weapon ETB

        // Germ is 0/0 with no swamps — dies to state-based actions
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        // Equipment stays on battlefield
        harness.assertOnBattlefield(player1, "Lashwrithe");
    }

    // ===== Boost per Swamp =====

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each Swamp controller controls")
    void boostsPerSwamp() {
        harness.setHand(player1, List.of(new Lashwrithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve living weapon ETB

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 3 swamps * +1/+1 = 3/3
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost updates dynamically when Swamp count changes")
    void updatesDynamicallyWithSwampCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent lashwrithe = harness.addToBattlefieldAndReturn(player1, new Lashwrithe());
        lashwrithe.setAttachedTo(bears.getId());

        // No swamps — bears is 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        // Remove all swamps
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts equipment controller's Swamps, not equipped creature's controller's")
    void countsEquipmentControllersSwamps() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Lashwrithe controlled by player1, attached to player2's creature
        Permanent lashwrithe = harness.addToBattlefieldAndReturn(player1, new Lashwrithe());
        lashwrithe.setAttachedTo(opponentBears.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        // Should count player1's 2 swamps, not player2's 3
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4); // 2 base + 2 swamps
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count opponent's Swamps")
    void doesNotCountOpponentSwamps() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent lashwrithe = harness.addToBattlefieldAndReturn(player1, new Lashwrithe());
        lashwrithe.setAttachedTo(bears.getId());

        // Only opponent has swamps
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        // No boost from opponent's swamps
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Equip to another creature =====

    @Test
    @DisplayName("Equipping Lashwrithe to another creature transfers the boost")
    void equipToAnotherCreature() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        Permanent lashwrithe = harness.addToBattlefieldAndReturn(player1, new Lashwrithe());

        // Simulate living weapon state: attach to a germ
        Permanent germ = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        germ.setSummoningSick(false);
        lashwrithe.setAttachedTo(germ.getId());

        // Find the lashwrithe permanent index
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int lashwritheIndex = battlefield.indexOf(lashwrithe);

        // Equip to bears
        harness.activateAbility(player1, lashwritheIndex, null, bears.getId());
        harness.passBothPriorities();

        assertThat(lashwrithe.getAttachedTo()).isEqualTo(bears.getId());

        // Bears gets +1/+1 per swamp (2 swamps)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);  // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);  // 2 + 2
    }

    // ===== Equipment stays when Germ dies =====

    @Test
    @DisplayName("Lashwrithe stays on battlefield when Germ is removed")
    void equipmentStaysWhenGermIsRemoved() {
        // Need a swamp so the Germ survives long enough to test removal
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Lashwrithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        gd.playerBattlefields.get(player1.getId()).remove(germ);

        harness.assertOnBattlefield(player1, "Lashwrithe");
    }
    @ParameterizedTest
    @CsvSource({"0, 16", "1, 18", "2, 20"})
    @DisplayName("Equip pays each Phyrexian symbol with black mana or two life")
    void equipPaysPhyrexianCost(int blackMana, int expectedLife) {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Lashwrithe());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, blackMana);

        harness.activateAbility(player1, 1, null, bears.getId());

        harness.assertLife(player1, expectedLife);
        assertThat(equipment.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Moving Lashwrithe from its Germ kills the Germ when it loses the boost")
    void movingEquipmentKillsGerm() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lashwrithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Lashwrithe");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(equipment), null, bears.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(germ);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
