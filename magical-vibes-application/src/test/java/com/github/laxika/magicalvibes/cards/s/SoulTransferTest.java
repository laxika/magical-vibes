package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulTransfer.class, GrizzlyBears.class, Millstone.class,
        FountainOfYouth.class, GloriousAnthem.class})
class SoulTransferTest extends BaseCardTest {

    @Test
    @DisplayName("Exile mode exiles a target creature")
    void exilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Return mode returns a target creature card from the graveyard")
    void returnsTargetCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes are available when an artifact and enchantment are controlled")
    void bothModesResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        cast(new int[]{0, 1}, List.of(target.getId(), creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes cannot be chosen without an artifact and enchantment")
    void bothModesRequireArtifactAndEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SoulTransfer()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(target.getId(), creature.getId()), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes");
    }

    @Test
    @DisplayName("Exile mode cannot target a noncreature permanent")
    void exileModeRejectsNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new SoulTransfer()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(target.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SoulTransfer()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
