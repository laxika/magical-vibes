package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuLair;
import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.cards.v.VodalianZombie;
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

@CardUsed({GohamDjinn.class, Forest.class, KavuLair.class, MetathranZombie.class,
        VodalianZombie.class})
class GohamDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Shrinks when black is the most common color")
    void shrinksWhenBlackIsMostCommon() {
        Permanent goham = addGohamDjinn();

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when black is tied for most common color")
    void shrinksWhenBlackIsTied() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, new MetathranZombie());
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts every color of a multicolored permanent")
    void countsEveryColorOfMulticoloredPermanent() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, new VodalianZombie());
        harness.addToBattlefield(player2, new MetathranZombie());
        harness.addToBattlefield(player2, new MetathranZombie());

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, new KavuLair());
        harness.addToBattlefield(player2, new KavuLair());

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count colorless permanents")
    void doesNotCountColorlessPermanents() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating the ability grants a regeneration shield")
    void activatingAbilityGrantsRegenerationShield() {
        Permanent goham = addGohamDjinn();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goham.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability requires black mana")
    void regenerationAbilityRequiresBlackMana() {
        addGohamDjinn();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Goham Djinn from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent goham = addGohamDjinn();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GohamDjinn());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Goham Djinn");
        assertThat(goham.isTapped()).isTrue();
        assertThat(goham.getRegenerationShield()).isZero();
    }

    private Permanent addGohamDjinn() {
        return harness.addToBattlefieldAndReturn(player1, new GohamDjinn());
    }
}
