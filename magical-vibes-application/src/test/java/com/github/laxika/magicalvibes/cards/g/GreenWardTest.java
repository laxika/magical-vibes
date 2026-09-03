package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.Lifelace;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

@CardUsed({GreenWard.class, GiantGrowth.class, GrizzlyBears.class, Lifelace.class, SolRing.class})
class GreenWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from green")
    void enchantedCreatureHasProtectionFromGreen() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GreenWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not gain protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GreenWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Green Ward leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GreenWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.GREEN)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Protection from green does not remove Green Ward when it becomes green")
    void protectionDoesNotRemoveGreenWardWhenWardBecomesGreen() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenWard());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gqs.getEffectiveColors(gd, aura)).containsExactly(CardColor.GREEN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GreenWard());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Resolving Green Ward attaches it and grants protection from green")
    void resolvesAndGrantsProtectionFromGreen() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GreenWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == GreenWard.class
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GreenWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GreenWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new GreenWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
