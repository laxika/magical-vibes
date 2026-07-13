package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OppositionTest extends BaseCardTest {

    // ===== Tapping targets =====

    @Test
    @DisplayName("Taps target creature by tapping a creature you control")
    void tapsTargetCreature() {
        addOpposition(player1);
        Permanent cost = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(cost.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap target land")
    void canTapTargetLand() {
        addOpposition(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addPermanent(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap target artifact")
    void canTapTargetArtifact() {
        addOpposition(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addPermanent(player2, new AngelsFeather());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addOpposition(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantment = addPermanent(player2, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== Cost: tapping a creature you control =====

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        addOpposition(player1);
        Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
        tapped.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With multiple untapped creatures, controller chooses which to tap")
    void multipleCreaturesChoice() {
        addOpposition(player1);
        Permanent bears1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bears2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears1.getId());
        harness.passBothPriorities();

        assertThat(bears1.isTapped()).isTrue();
        assertThat(bears2.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addOpposition(Player player) {
        return addPermanent(player, new Opposition());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
