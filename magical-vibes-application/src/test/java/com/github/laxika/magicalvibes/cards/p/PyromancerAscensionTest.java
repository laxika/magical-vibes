package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyromancerAscension.class, BurstLightning.class, Demolish.class, Mountain.class})
class PyromancerAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("A matching instant or sorcery offers a quest counter")
    void matchingSpellOffersQuestCounter() {
        var ascension = addAscension(0);
        harness.setGraveyard(player1, List.of(new BurstLightning()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell without a same-name card in the graveyard does not add a counter")
    void differentNameDoesNotOfferQuestCounter() {
        var ascension = addAscension(0);
        harness.setGraveyard(player1, List.of(new PyromancerAscension()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Two quest counters allow an instant or sorcery to be copied")
    void twoCountersAllowCopy() {
        addAscension(2);
        harness.setGraveyard(player1, List.of(new PyromancerAscension()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.getCard().getName().equals("Burst Lightning")).count())
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    void removingCountersAfterCastingDoesNotPreventCopy() {
        var ascension = addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        ascension.setCounterCount(CounterType.QUEST, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).hasSize(1);
    }

    @Test
    void questCounterCanBeDeclined() {
        var ascension = addAscension(0);
        harness.setGraveyard(player1, List.of(new BurstLightning()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void copyCanBeDeclined() {
        addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reachingTwoCountersDoesNotCopyTheSpellThatAddedTheSecond() {
        var ascension = addAscension(1);
        harness.setGraveyard(player1, List.of(new BurstLightning()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void removingTheMatchingGraveyardCardDoesNotPreventTheCounter() {
        var ascension = addAscension(0);
        harness.setGraveyard(player1, List.of(new BurstLightning()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void opponentsGraveyardDoesNotQualify() {
        var ascension = addAscension(0);
        harness.setGraveyard(player2, List.of(new BurstLightning()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void matchingEnchantmentDoesNotTriggerEitherAbility() {
        var ascension = addAscension(2);
        harness.setGraveyard(player1, List.of(new PyromancerAscension()));
        harness.setHand(player1, List.of(new PyromancerAscension()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.stack.stream().filter(entry -> entry.isCopy())).isEmpty();
    }

    @Test
    void matchingSorceryOffersQuestCounter() {
        var ascension = addAscension(0);
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new Demolish()));
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void kickedSpellCopyRetainsKickerAndDoesNotTriggerAscensionAgain() {
        var ascension = addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void optionalCounterChoiceWaitsForTriggerResolution() {
        addAscension(0);
        harness.setGraveyard(player1, List.of(new BurstLightning()));
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    void optionalCopyChoiceWaitsForTriggerResolution() {
        addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    void copyMayChooseANewTargetWithoutChangingOriginal() {
        addAscension(2);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    private com.github.laxika.magicalvibes.model.Permanent addAscension(int counters) {
        var ascension = harness.addToBattlefieldAndReturn(player1, new PyromancerAscension());
        ascension.setCounterCount(CounterType.QUEST, counters);
        return ascension;
    }
}
