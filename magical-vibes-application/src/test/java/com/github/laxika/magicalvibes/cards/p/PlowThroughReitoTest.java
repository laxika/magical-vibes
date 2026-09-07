package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlowThroughReitoTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen Plains and boosts the target creature by their number")
    void returnsChosenPlainsAndBoostsTargetCreature() {
        Permanent target = addCreature(player2);
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castCard(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(firstPlains.getId(), secondPlains.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstPlains.getId(), secondPlains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest)
                .doesNotContain(firstPlains, secondPlains);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returning no Plains gives no boost and is legal")
    void returningNoPlainsGivesNoBoost() {
        Permanent target = addCreature(player2);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        castCard(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new PlowThroughReito()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");
    }

    private void castCard(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new PlowThroughReito()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0, targetId);
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
