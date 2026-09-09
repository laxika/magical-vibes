package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteWidowFreeAgent.class, FountainOfYouth.class, GloriousAnthem.class,
        GrizzlyBears.class, Island.class})
class WhiteWidowFreeAgentTest extends BaseCardTest {

    @Test
    void counterModePutsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCounterMode(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterModeCannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> castCounterMode(List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnModeReturnsArtifactCard() {
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature, artifact)));

        castReturnMode();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(artifact.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnModeReturnsEnchantmentCard() {
        Card enchantment = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature, enchantment)));

        castReturnMode();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(enchantment.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castCounterMode(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WhiteWidowFreeAgent()));
        addMana();
        harness.castCreature(player1, 0, targetIds);
    }

    private void castReturnMode() {
        harness.setHand(player1, List.of(new WhiteWidowFreeAgent()));
        addMana();
        harness.castCreature(player1, 0, 1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
