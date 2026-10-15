package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoisedPractitioner.class, Forest.class, LightningBolt.class})
class PoisedPractitionerTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell puts a +1/+1 counter on Poised Practitioner and scries 1")
    void secondSpellPutsCounterAndScries() {
        Permanent practitioner = addCreatureReady(player1, new PoisedPractitioner());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(practitioner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(practitioner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The third spell does not trigger Poised Practitioner's flurry again")
    void thirdSpellDoesNotTriggerAgain() {
        Permanent practitioner = addCreatureReady(player1, new PoisedPractitioner());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(practitioner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void opponentSpellsDoNotTriggerFlurry() {
        Permanent practitioner = addCreatureReady(player1, new PoisedPractitioner());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(practitioner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countsSpellCastBeforePractitionerEntered() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        Permanent practitioner = addCreatureReady(player1, new PoisedPractitioner());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(practitioner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
    }

    @Test
    void emptyLibraryDoesNotPreventCounter() {
        Permanent practitioner = addCreatureReady(player1, new PoisedPractitioner());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(practitioner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillScriesWhenPractitionerDiesBeforeTriggerResolves() {
        Permanent practitioner = addCreatureReady(player1, new PoisedPractitioner());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of(new LightningBolt()));
        Forest topCard = new Forest();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, practitioner.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(practitioner);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
    }
}
