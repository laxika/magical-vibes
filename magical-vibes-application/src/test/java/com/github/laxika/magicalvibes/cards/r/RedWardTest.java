package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from red")
    void enchantedCreatureHasProtectionFromRed() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new RedWard());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not gain protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new RedWard());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Protection is lost when Red Ward leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new RedWard());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasProtectionFrom(gd, bearsPerm, CardColor.RED)).isFalse();
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
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RedWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
