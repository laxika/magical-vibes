package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaGodsScorn.class, AngelicChorus.class, GrizzlyBears.class, Island.class})
class SeaGodsScornTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three target creatures and enchantments to their owners' hands")
    void returnsThreeMixedTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());

        cast(List.of(ownCreature.getId(), opponentCreature.getId(), enchantment.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Angelic Chorus");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast with fewer than three targets")
    void returnsOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicChorus());

        cast(List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AngelicChorus)
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castsWithNoTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures or enchantments");
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
