package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchenemysCharm.class, ChandraNalaar.class, FountainOfYouth.class, GrizzlyBears.class})
class ArchenemysCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Exile target creature or planeswalker")
    class ExileMode {

        @Test
        @DisplayName("Exiles a target creature")
        void exilesCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castCharm(0, harness.getPermanentId(player2, "Grizzly Bears"));

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Exiles a target planeswalker")
        void exilesPlaneswalker() {
            Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
            castCharm(0, planeswalker.getId());

            harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        }

        @Test
        @DisplayName("Cannot target a noncreature nonplaneswalker permanent")
        void rejectsOtherPermanent() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new ArchenemysCharm()));
            addMana();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Return one or two target creature and/or planeswalker cards")
    class ReturnMode {

        @Test
        @DisplayName("Returns one creature and one planeswalker from the graveyard")
        void returnsTwoMatchingCards() {
            Card creature = new GrizzlyBears();
            Card planeswalker = new ChandraNalaar();
            Card other = new FountainOfYouth();
            harness.setGraveyard(player1, List.of(creature, planeswalker, other));
            harness.setHand(player1, List.of(new ArchenemysCharm()));
            addMana();

            harness.castInstant(player1, 0, 1, null);

            PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                    .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), planeswalker.getId());

            harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), planeswalker.getId()));
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .extracting(Card::getId)
                    .contains(creature.getId(), planeswalker.getId())
                    .doesNotContain(other.getId());
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .extracting(Card::getId)
                    .contains(other.getId())
                    .doesNotContain(creature.getId(), planeswalker.getId());
        }
    }

    @Nested
    @DisplayName("Mode 2: Put two +1/+1 counters on target creature you control")
    class CounterMode {

        @Test
        @DisplayName("Adds counters and grants lifelink only until end of turn")
        void addsCountersAndLifelink() {
            Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            castCharm(2, ownCreature.getId());

            assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
            assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
            assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
        }

        @Test
        @DisplayName("Cannot target a creature controlled by an opponent")
        void rejectsOpponentCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new ArchenemysCharm()));
            addMana();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castCharm(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ArchenemysCharm()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
