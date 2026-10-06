package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Chaoslace;
import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedWard.class, Chaoslace.class, GrizzlyBears.class, LightningBolt.class,
        AladdinsRing.class, Earthquake.class, HillGiant.class})
class RedWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from red")
    void enchantedCreatureHasProtectionFromRed() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new RedWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not gain protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new RedWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Red Ward leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new RedWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection from red does not remove Red Ward when it becomes red")
    void protectionDoesNotRemoveRedWardWhenWardBecomesRed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedWard());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gqs.getEffectiveColors(gd, aura)).containsExactly(CardColor.RED);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedWard());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Resolving Red Ward attaches it and grants protection from red")
    void resolvesAndGrantsProtectionFromRed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == RedWard.class
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AladdinsRing());
        harness.setHand(player1, List.of(new RedWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Protection prevents damage from a red spell that does not target")
    void preventsNontargetedRedDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedWard());
        aura.setAttachedTo(bears.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, aura);
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Red creatures cannot block the enchanted creature, but green creatures can")
    void preventsRedBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RedWard());
        aura.setAttachedTo(attacker.getId());
        Permanent redBlocker = addCreatureReady(player2, new HillGiant());
        Permanent greenBlocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, redBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Independent protection from another Red Ward removes a Red Ward that becomes red")
    void independentProtectionRemovesRedAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent whiteAura = harness.addToBattlefieldAndReturn(player1, new RedWard());
        whiteAura.setAttachedTo(bears.getId());
        Permanent redAura = harness.addToBattlefieldAndReturn(player1, new RedWard());
        redAura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, redAura.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, whiteAura)
                .doesNotContain(redAura);
        harness.assertInGraveyard(player1, "Red Ward");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
    }
}
