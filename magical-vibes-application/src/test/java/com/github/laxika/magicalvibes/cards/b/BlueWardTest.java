package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheRack;
import com.github.laxika.magicalvibes.cards.t.Thoughtlace;
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

@CardUsed({BlueWard.class, GrizzlyBears.class, TheRack.class, Thoughtlace.class})
class BlueWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from blue")
    void enchantedCreatureHasProtectionFromBlue() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BlueWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not gain protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BlueWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Blue Ward leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new BlueWard());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLUE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Protection from blue does not remove Blue Ward when it becomes blue")
    void protectionDoesNotRemoveBlueWardWhenWardBecomesBlue() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlueWard());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new Thoughtlace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gqs.getEffectiveColors(gd, aura)).containsExactly(CardColor.BLUE);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Resolving Blue Ward attaches it and grants protection from blue")
    void resolvesAndGrantsProtectionFromBlue() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlueWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == BlueWard.class
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlueWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlueWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TheRack());
        harness.setHand(player1, List.of(new BlueWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
