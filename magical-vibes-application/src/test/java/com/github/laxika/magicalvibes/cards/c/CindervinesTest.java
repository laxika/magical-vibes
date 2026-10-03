package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.u.UnbreakableFormation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cindervines.class, Divination.class, GloriousAnthem.class, GrizzlyBears.class, Millstone.class,
        SphinxOfTheGuildpact.class, UnbreakableFormation.class})
class CindervinesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to an opponent who casts a noncreature spell")
    void damagesOpponentCastingNoncreatureSpell() {
        harness.addToBattlefield(player1, new Cindervines());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a creature spell")
    void doesNotDamageOpponentCastingCreatureSpell() {
        harness.addToBattlefield(player1, new Cindervines());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Sacrificing Cindervines destroys an artifact and damages its controller")
    void destroysArtifactAndDamagesItsController() {
        harness.addToBattlefield(player1, new Cindervines());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cindervines");
        harness.assertInGraveyard(player1, "Cindervines");
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Sacrificing Cindervines destroys an enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player1, new Cindervines());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new Cindervines());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("The controller's noncreature spells do not trigger Cindervines")
    void doesNotDamageControllerCastingNoncreatureSpell() {
        harness.addToBattlefield(player1, new Cindervines());
        harness.setHand(player1, List.of(new Cindervines()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.battlefield.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Damage from an enchantment cast resolves before the enchantment")
    void triggersBeforeOpponentEnchantmentResolves() {
        harness.addToBattlefield(player1, new Cindervines());
        harness.setHand(player2, List.of(new Cindervines()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Cindervines");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cindervines");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cindervines can target itself and is sacrificed without dealing damage")
    void selfTargetIsIllegalAfterPayingSacrificeCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Cindervines());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, source.getId());

        harness.assertNotOnBattlefield(player1, "Cindervines");
        harness.assertInGraveyard(player1, "Cindervines");

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroying another enchantment you control deals damage to you")
    void canDestroyOwnEnchantmentAndDamageController() {
        harness.addToBattlefield(player1, new Cindervines());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Cindervines());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.battlefield.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No damage is dealt when the target is sacrificed before resolution")
    void doesNotDamageControllerOfTargetThatLeavesBattlefield() {
        harness.addToBattlefield(player1, new Cindervines());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cindervines());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cindervines");
        harness.assertInGraveyard(player2, "Cindervines");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An indestructible artifact survives but its controller still takes damage")
    void damagesControllerEvenWhenArtifactCannotBeDestroyed() {
        harness.addToBattlefield(player1, new Cindervines());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheGuildpact());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sphinx of the Guildpact");
        harness.assertNotInGraveyard(player1, "Sphinx of the Guildpact");
        harness.assertInGraveyard(player1, "Cindervines");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
