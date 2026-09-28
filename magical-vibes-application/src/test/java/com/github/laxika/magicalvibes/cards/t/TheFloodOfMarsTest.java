package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFloodOfMars.class, Forest.class, GrizzlyBears.class})
class TheFloodOfMarsTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger targets another creature or land")
    void attackTriggerOffersAnotherCreatureOrLand() {
        Permanent flood = addCreatureReady(player1, new TheFloodOfMars());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());
        assertThat(choice.validIds()).doesNotContain(flood.getId());
    }

    @Test
    @DisplayName("The attack trigger floods and copies the target creature")
    void floodsAndCopiesTargetCreature() {
        addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(target.getCard().getName()).isEqualTo("The Flood of Mars");
        assertThat(target.getCard().getPower()).isEqualTo(3);
        assertThat(target.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack trigger floods a land and adds Island to its types")
    void floodsAndAddsIslandToTargetLand() {
        addCreatureReady(player1, new TheFloodOfMars());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveLandTypes(gd, target))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
    }
}
