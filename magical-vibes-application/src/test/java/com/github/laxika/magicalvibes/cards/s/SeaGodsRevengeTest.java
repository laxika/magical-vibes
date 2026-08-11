package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeaGodsRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three opposing creatures and then scries 1")
    void returnsThreeOpposingCreaturesAndScries() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        harness.castSorcery(player1, 0, List.of(bears.getId(), spider.getId(), elemental.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
        harness.assertInHand(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player1, "Sea God's Revenge");
    }

    @Test
    @DisplayName("Can resolve with no targets")
    void canResolveWithNoTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
