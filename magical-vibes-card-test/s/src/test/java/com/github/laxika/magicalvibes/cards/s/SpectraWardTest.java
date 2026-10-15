package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.e.Encrust;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({SpectraWard.class, RuneclawBear.class, DarksteelCitadel.class, Encrust.class})
class SpectraWardTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpectraWard());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Resolving Spectra Ward attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new SpectraWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SpectraWard
                        && p.isAttached()
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and protection from every color")
    void enchantedCreatureGetsBoostAndProtectionFromEveryColor() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, bears, color)).isTrue();
        }
    }

    @Test
    @DisplayName("Its protection does not remove Spectra Ward")
    void protectionDoesNotRemoveAura() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = enchant(bears);

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Removing Spectra Ward removes its bonuses")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = enchant(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, bears, color)).isFalse();
        }
    }

    @Test
    @DisplayName("Spectra Ward preserves another player's colored Aura already attached to the creature")
    void existingColoredAuraRemainsAttached() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent encrust = harness.addToBattlefieldAndReturn(player2, new Encrust());
        encrust.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new SpectraWard()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(encrust);
        assertThat(encrust.getAttachedTo()).isEqualTo(bears.getId());
        harness.assertNotInGraveyard(player2, "Encrust");
        harness.assertOnBattlefield(player1, "Spectra Ward");
    }

    @Test
    @DisplayName("Protection still prevents targeting with subsequent colored Aura spells")
    void cannotTargetWithAnotherColoredAura() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        enchant(bears);
        harness.setHand(player1, List.of(new Encrust()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Encrust");
    }

    @Test
    @DisplayName("Two attached Spectra Wards preserve each other")
    void multipleWardsRemainAttached() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent first = enchant(bears);
        Permanent second = enchant(bears);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Spectra Ward cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.setHand(player1, List.of(new SpectraWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent artifact = findPermanent(player1, "Darksteel Citadel");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
