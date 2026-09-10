package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DefenseGrid;
import com.github.laxika.magicalvibes.cards.d.DefenseOfTheHeart;
import com.github.laxika.magicalvibes.cards.f.ForbiddingWatchtower;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RingOfGix.class, DefenseGrid.class, GiantCockroach.class,
        ForbiddingWatchtower.class, DefenseOfTheHeart.class})
class RingOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target artifact")
    void tapsTargetArtifact() {
        Permanent ring = addReadyRing();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DefenseGrid());

        activate(ring, target);

        assertThat(ring.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a target creature")
    void tapsTargetCreature() {
        Permanent ring = addReadyRing();
        Permanent target = addCreatureReady(player2, new GiantCockroach());

        activate(ring, target);

        assertThat(ring.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a target land")
    void tapsTargetLand() {
        Permanent ring = addReadyRing();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForbiddingWatchtower());

        activate(ring, target);

        assertThat(ring.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent ring = addReadyRing();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DefenseOfTheHeart());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    @Test
    @DisplayName("Activation consumes one generic mana")
    void activationConsumesOneGenericMana() {
        Permanent ring = addReadyRing();
        Permanent target = addCreatureReady(player2, new GiantCockroach());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null,
                target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        Permanent ring = addReadyRing();
        Permanent target = addCreatureReady(player2, new GiantCockroach());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while Ring of Gix is tapped")
    void cannotActivateWhenTapped() {
        Permanent ring = addReadyRing();
        Permanent target = addCreatureReady(player2, new GiantCockroach());
        ring.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Artifact can activate its tap ability despite summoning sickness")
    void artifactCanActivateDespiteSummoningSickness() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfGix());
        Permanent target = addCreatureReady(player2, new GiantCockroach());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null,
                target.getId());

        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining echo sacrifices Ring of Gix at its next upkeep")
    void decliningEchoSacrificesRingOfGix() {
        castAndResolveRingOfGix();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ring of Gix");
        harness.assertInGraveyard(player1, "Ring of Gix");
    }

    @Test
    @DisplayName("Paying echo keeps Ring of Gix and echo does not trigger again")
    void payingEchoKeepsRingOfGixAndIsOneShot() {
        castAndResolveRingOfGix();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ring of Gix");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Ring of Gix");
    }

    private Permanent addReadyRing() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfGix());
        ring.setSummoningSick(false);
        return ring;
    }

    private void activate(Permanent ring, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null,
                target.getId());
        harness.passBothPriorities();
    }

    private void castAndResolveRingOfGix() {
        harness.setHand(player1, List.of(new RingOfGix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ring of Gix");
    }
}
