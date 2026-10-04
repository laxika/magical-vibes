package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HighSpeedHoverbike;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.cards.n.NinjasKunai;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FavorOfJukai.class, NinjasKunai.class, Forest.class, JukaiPreserver.class, HighSpeedHoverbike.class})
class FavorOfJukaiTest extends BaseCardTest {

    @Test
    @DisplayName("Favor of Jukai boosts an enchanted creature and grants reach")
    void enchantedCreatureGetsBoostAndReach() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JukaiPreserver());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        addAuraMana();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Favor of Jukai can enchant an artifact without granting creature bonuses")
    void artifactGetsNoCreatureBonus() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        addAuraMana();

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.REACH)).isFalse();
        harness.assertOnBattlefield(player1, "Favor of Jukai");
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).getAttachedTo()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Favor of Jukai cannot enchant a land")
    void cannotEnchantLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        addAuraMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Channel gives a target creature +3/+3 and reach until end of turn")
    void channelBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JukaiPreserver());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Favor of Jukai");
        harness.assertNotInHand(player1, "Favor of Jukai");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
        harness.assertInGraveyard(player1, "Favor of Jukai");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }

    @Test
    void auraCanEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JukaiPreserver());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        addAuraMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void channelCanTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JukaiPreserver());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void channelCannotTargetNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Favor of Jukai");
        harness.assertNotInGraveyard(player1, "Favor of Jukai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelRequiresGreenMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiPreserver());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Favor of Jukai");
        harness.assertNotInGraveyard(player1, "Favor of Jukai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enchantedVehicleGainsBonusesOnlyWhileCreature() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new HighSpeedHoverbike());
        harness.addToBattlefield(player1, new JukaiPreserver());
        harness.setHand(player1, List.of(new FavorOfJukai()));
        addAuraMana();

        harness.castEnchantment(player1, 0, vehicle.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.REACH)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.REACH)).isFalse();
        harness.assertOnBattlefield(player1, "Favor of Jukai");
    }

    private void addAuraMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
