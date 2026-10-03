package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeconstructionHammer.class, GrizzlyBears.class, GloriousAnthem.class, LeoninScimitar.class})
class DeconstructionHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature can sacrifice Deconstruction Hammer to destroy an artifact")
    void destroysTargetArtifact() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deconstruction Hammer");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Equipped creature can destroy an enchantment")
    void destroysTargetEnchantment() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Granted ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipAttachesAndMovingItRemovesTheOldBoost() {
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, first.getId());
        harness.passBothPriorities();
        assertThat(hammer.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();
        assertThat(hammer.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        addReadyHammer(player1);
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeAndTapArePaidBeforeResolutionAndRemoveTheBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Deconstruction Hammer");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = addReadyCreature(player1);
        creature.setSummoningSick(true);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Deconstruction Hammer");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotActivateGrantedAbility() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Deconstruction Hammer");
    }

    @Test
    void cannotActivateWithoutThreeMana() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Deconstruction Hammer");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void cannotSacrificeAHammerControlledByAnotherPlayer() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player2);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Deconstruction Hammer");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void canTargetTheHammerItselfButItIsAlreadySacrificedOnResolution() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, hammer.getId());
        harness.assertInGraveyard(player1, "Deconstruction Hammer");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyHammer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeconstructionHammer());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GloriousAnthem());
    }
}
