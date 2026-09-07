package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TithingBlade.class, ConsumingSepulcher.class, GrizzlyBears.class})
class TithingBladeTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent sacrifices a creature of their choice")
    void eachOpponentSacrificesCreatureOnEnter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TithingBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, secondCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Craft exiles a creature and returns Consuming Sepulcher transformed")
    void craftsWithCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new TithingBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blade, creature);
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof ConsumingSepulcher);
    }

    @Test
    @DisplayName("Craft exiles a creature card from the graveyard and returns transformed")
    void craftsWithCreatureFromGraveyard() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new TithingBlade());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blade);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof ConsumingSepulcher);
    }

    @Test
    @DisplayName("Consuming Sepulcher drains each opponent during its controller's upkeep")
    void consumingSepulcherUpkeepDrainsOpponent() {
        addTransformedSepulcher();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private Permanent addTransformedSepulcher() {
        TithingBlade front = new TithingBlade();
        Permanent sepulcher = new Permanent(front);
        sepulcher.setCard(front.getBackFaceCard());
        sepulcher.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(sepulcher);
        return sepulcher;
    }
}
