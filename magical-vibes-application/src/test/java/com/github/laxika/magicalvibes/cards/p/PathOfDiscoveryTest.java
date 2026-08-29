package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under your control explores and puts a revealed land into your hand")
    void enteringCreatureExploresLand() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(land.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature entering under your control explores and gets a counter for a revealed nonland")
    void enteringCreatureExploresNonland() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfDiscovery());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(path.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
    }

    private Permanent castEnteringCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card creatureCard = new GrizzlyBears();
        harness.setHand(player1, List.of(creatureCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
    }
}
