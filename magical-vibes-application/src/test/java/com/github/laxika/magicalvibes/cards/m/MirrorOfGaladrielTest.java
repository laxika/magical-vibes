package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorOfGaladriel.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class MirrorOfGaladrielTest extends BaseCardTest {

    @Test
    @DisplayName("Scries and draws with its activation cost reduced by legendary creatures")
    void scriesAndDrawsWithLegendaryCostReduction() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new MirrorOfGaladriel());
        addLegendaryCreature(player1, new GrizzlyBears());
        addLegendaryCreature(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(mirror.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not count legendary creatures controlled by an opponent")
    void doesNotCountOpponentsLegendaryCreatures() {
        harness.addToBattlefield(player1, new MirrorOfGaladriel());
        addLegendaryCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addLegendaryCreature(Player player, Card creature) {
        creature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.addToBattlefield(player, creature);
    }
}
