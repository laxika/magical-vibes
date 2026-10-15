package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientCarp;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DragonlordDromoka;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MythRealized;
import com.github.laxika.magicalvibes.cards.t.TerritorialRoc;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.cards.u.UpdraftElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendingVolley.class, Cancel.class, EliteVanguard.class, FugitiveWizard.class, GrizzlyBears.class,
        AncientCarp.class, TerritorialRoc.class, UltimatePrice.class, UpdraftElemental.class,
        DragonlordDromoka.class, MythRealized.class})
class RendingVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a white creature")
    void dealsDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Deals 4 damage to a blue creature")
    void dealsDamageToBlueCreature() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Fugitive Wizard"));

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot target a creature that is not white or blue")
    void cannotTargetNonWhiteOrBlueCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new EliteVanguard());
        RendingVolley rendingVolley = new RendingVolley();
        harness.setHand(player1, List.of(rendingVolley));
        addRendingVolleyMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rendingVolley.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Deals exactly four damage to a creature with five toughness")
    void dealsExactlyFourDamage() {
        var target = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Ancient Carp");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Rending Volley");
    }

    @Test
    @DisplayName("Four damage is lethal to a creature with four toughness")
    void killsCreatureWithFourToughness() {
        var target = harness.addToBattlefieldAndReturn(player2, new UpdraftElemental());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Updraft Elemental");
        harness.assertInGraveyard(player2, "Updraft Elemental");
    }

    @Test
    @DisplayName("Can target a white creature you control")
    void canTargetOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new TerritorialRoc());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Territorial Roc");
        harness.assertInGraveyard(player1, "Territorial Roc");
    }

    @Test
    @DisplayName("Does not resolve when its only target leaves the battlefield")
    void doesNotResolveWithMissingTarget() {
        var target = harness.addToBattlefieldAndReturn(player2, new TerritorialRoc());
        var otherCreature = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        harness.setHand(player1, List.of(new RendingVolley()));
        harness.setHand(player2, List.of(new UltimatePrice()));
        addRendingVolleyMana();
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Territorial Roc");
        harness.assertInGraveyard(player2, "Ultimate Price");
        harness.assertInGraveyard(player1, "Rending Volley");
        harness.assertOnBattlefield(player2, "Ancient Carp");
        assertThat(otherCreature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a multicolored creature that is white")
    void canTargetMulticoloredWhiteCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new DragonlordDromoka());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Dragonlord Dromoka");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a white permanent that is not a creature")
    void cannotTargetWhiteNoncreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new MythRealized());
        harness.setHand(player1, List.of(new RendingVolley()));
        addRendingVolleyMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addRendingVolleyMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
