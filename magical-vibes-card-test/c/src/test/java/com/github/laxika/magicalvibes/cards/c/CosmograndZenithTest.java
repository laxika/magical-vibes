package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmograndZenith.class, GrizzlyBears.class, Shock.class})
class CosmograndZenithTest extends BaseCardTest {

    private static final String TOKEN_MODE = "Create two 1/1 white Human Soldier creature tokens.";
    private static final String COUNTER_MODE = "Put a +1/+1 counter on each creature you control.";

    @Test
    @DisplayName("The second spell can create two Human Soldier tokens")
    void createsHumanSoldierTokens() {
        harness.addToBattlefield(player1, new CosmograndZenith());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castTwoSpellsAndChoose(TOKEN_MODE);

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        }
    }

    @Test
    @DisplayName("The second spell can put a +1/+1 counter on each creature you control")
    void putsCountersOnControlledCreatures() {
        Permanent zenith = harness.addToBattlefieldAndReturn(player1, new CosmograndZenith());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castTwoSpellsAndChoose(COUNTER_MODE);

        assertThat(zenith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only the second spell triggers, not the first or third")
    void onlySecondSpellTriggers() {
        harness.addToBattlefield(player1, new CosmograndZenith());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, TOKEN_MODE);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Spells cast before Zenith enters still count")
    void countsSpellsCastBeforeEntering() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new CosmograndZenith());

        harness.castInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, TOKEN_MODE);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
    }

    @Test
    @DisplayName("Zenith itself counts as the first spell and the trigger precedes the second creature")
    void countsItselfAndResolvesBeforeSecondCreature() {
        harness.setHand(player1, List.of(new CosmograndZenith(), new CosmograndZenith()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent firstZenith = findPermanent(player1, "Cosmogrand Zenith");
        assertThat(firstZenith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player1, 0);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.passBothPriorities();

        assertThat(firstZenith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Cosmogrand Zenith")).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Cosmogrand Zenith")).hasSize(2);
        assertThat(findPermanents(player1, "Cosmogrand Zenith")
                .stream().filter(p -> p != firstZenith).findFirst().orElseThrow()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller's second spell also triggers during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new CosmograndZenith());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castTwoSpellsAndChoose(TOKEN_MODE);

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
        assertThat(countPermanents(player2, "Human Soldier")).isZero();
    }

    @Test
    @DisplayName("Opponent spells neither trigger Zenith nor count toward its controller's second spell")
    void ignoresOpponentSpells() {
        harness.addToBattlefield(player1, new CosmograndZenith());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.castInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, TOKEN_MODE);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
    }

    @Test
    @DisplayName("The chosen counter mode resolves after Zenith dies")
    void counterModeResolvesAfterSourceDies() {
        Permanent zenith = harness.addToBattlefieldAndReturn(player1, new CosmograndZenith());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, COUNTER_MODE);

        harness.castAndResolveInstant(player2, 0, zenith.getId());
        harness.castAndResolveInstant(player2, 0, zenith.getId());
        assertThat(findPermanents(player1, "Cosmogrand Zenith")).isEmpty();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    private void castTwoSpellsAndChoose(String mode) {
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, mode);
        resolveAllTriggers();
    }
}
