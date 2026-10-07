package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.DwarvenNomad;
import com.github.laxika.magicalvibes.cards.f.Firebreathing;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SubterraneanSpirit.class, BayFalcon.class, DwarvenNomad.class, ZhalfirinKnight.class,
        Boomerang.class, Firebreathing.class, Incinerate.class})
class SubterraneanSpiritTest extends BaseCardTest {

    private Permanent addReadySpirit() {
        return addCreatureReady(player1, new SubterraneanSpirit());
    }

    @Test
    @DisplayName("Deals 1 damage to each creature without flying, sparing flyers")
    void damagesOnlyNonFlyers() {
        addReadySpirit();
        harness.addToBattlefield(player1, new DwarvenNomad());    // 1/1 non-flying -> dies
        harness.addToBattlefield(player2, new DwarvenNomad());    // 1/1 non-flying -> dies
        harness.addToBattlefield(player2, new BayFalcon());       // 1/1 flying -> survives
        harness.addToBattlefield(player2, new ZhalfirinKnight()); // 2/2 non-flying -> survives

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dwarven Nomad");
        harness.assertNotOnBattlefield(player2, "Dwarven Nomad");
        harness.assertOnBattlefield(player2, "Bay Falcon");
        harness.assertOnBattlefield(player2, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Protection from red prevents the damage from its own red ability")
    void protectionFromRedPreventsSelfDamage() {
        Permanent spirit = addReadySpirit();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Subterranean Spirit");
        assertThat(spirit.isTapped()).isTrue();
        assertThat(spirit.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        addReadySpirit();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void dealsExactlyOneDamageToSurvivingNonFlyersOnBothSides() {
        addReadySpirit();
        Permanent ownKnight = harness.addToBattlefieldAndReturn(player1, new ZhalfirinKnight());
        Permanent opposingKnight = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        Permanent opposingSpirit = harness.addToBattlefieldAndReturn(player2, new SubterraneanSpirit());
        harness.setLife(player1, 17);
        harness.setLife(player2, 13);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownKnight.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingKnight.getMarkedDamage()).isEqualTo(1);
        assertThat(flyer.getMarkedDamage()).isZero();
        assertThat(opposingSpirit.getMarkedDamage()).isZero();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 13);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SubterraneanSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(spirit.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent spirit = addReadySpirit();
        spirit.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void redCreatureCannotBlock() {
        Permanent spirit = addReadySpirit();
        spirit.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DwarvenNomad());
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void cannotBeTargetedByRedInstant() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new SubterraneanSpirit());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBeEnchantedByRedAura() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new SubterraneanSpirit());
        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attachedRedAuraIsRemovedByStateBasedActions() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new SubterraneanSpirit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Firebreathing());
        aura.setAttachedTo(spirit.getId());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Firebreathing");
        harness.assertNotOnBattlefield(player1, "Firebreathing");
        harness.assertOnBattlefield(player2, "Subterranean Spirit");
    }

    @Test
    void abilityStillResolvesAfterSourceReturnsToHand() {
        Permanent spirit = addReadySpirit();
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());
        Permanent opposingSpirit = harness.addToBattlefieldAndReturn(player2, new SubterraneanSpirit());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, spirit.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Subterranean Spirit");
        harness.assertInHand(player1, "Subterranean Spirit");
        assertThat(knight.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingSpirit.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
