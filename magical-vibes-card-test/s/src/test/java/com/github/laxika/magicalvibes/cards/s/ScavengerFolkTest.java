package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.v.VernalBloom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengerFolk.class, SisaysRing.class, PhyrexianHulk.class, GrizzlyBears.class, VernalBloom.class, Forest.class})
class ScavengerFolkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Scavenger Folk and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scavenger Folk");
        harness.assertInGraveyard(player1, "Scavenger Folk");
        harness.assertNotOnBattlefield(player2, "Sisay's Ring");
        harness.assertInGraveyard(player2, "Sisay's Ring");
    }

    @Test
    @DisplayName("Sacrifices Scavenger Folk as an activation cost")
    void sacrificesAsActivationCost() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Scavenger Folk");
        harness.assertInGraveyard(player1, "Scavenger Folk");
        harness.assertOnBattlefield(player2, "Sisay's Ring");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sisay's Ring");
    }

    @Test
    @DisplayName("Can target own artifact")
    void canTargetOwnArtifact() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sisay's Ring");
    }

    @Test
    @DisplayName("Can target an artifact creature")
    void canTargetArtifactCreature() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifactCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Hulk");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (tap cost)")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ScavengerFolk());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent folk = addReadyFolk(player1);
        folk.tap();
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyFolk(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyFolk(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyFolk(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target artifact leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Pays exactly one green mana when activated")
    void paysOneGreenMana() {
        addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Scavenger Folk");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sisay's Ring");
    }

    @Test
    @DisplayName("Colorless mana cannot pay the green activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent folk = addReadyFolk(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(folk.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Scavenger Folk");
        harness.assertNotInGraveyard(player1, "Scavenger Folk");
        harness.assertOnBattlefield(player2, "Sisay's Ring");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal target does not consume mana, tap, or sacrifice Scavenger Folk")
    void illegalTargetDoesNotPayCosts() {
        Permanent folk = addReadyFolk(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(folk.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Scavenger Folk");
        harness.assertNotInGraveyard(player1, "Scavenger Folk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyFolk(Player player) {
        return addCreatureReady(player, new ScavengerFolk());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SisaysRing());
    }

    private Permanent addReadyArtifactCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PhyrexianHulk());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VernalBloom());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
