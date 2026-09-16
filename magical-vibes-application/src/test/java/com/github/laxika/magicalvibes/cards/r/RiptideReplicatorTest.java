package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideReplicator.class, Naturalize.class})
class RiptideReplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X charge counters and stores the chosen color and type")
    void entersWithCountersAndChoices() {
        Permanent replicator = castReplicatorWithChoices(3);

        assertThat(replicator.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(replicator.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(replicator.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creates a token sized to the current charge counters")
    void createsChosenColorAndTypeTokenSizedToCounters() {
        Permanent replicator = harness.addToBattlefieldAndReturn(player1, new RiptideReplicator());
        replicator.setChosenColor(CardColor.RED);
        replicator.setChosenSubtype(CardSubtype.GOBLIN);
        replicator.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        replicator.setCounterCount(CounterType.CHARGE, 5);
        harness.passBothPriorities();

        Permanent token = findToken(player1);
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(replicator.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("With X equal to zero, the created 0/0 token does not survive state-based actions")
    void zeroChargeCountersCreateNoSurvivingToken() {
        Permanent replicator = castReplicatorWithChoices(0);
        assertThat(replicator.getCounterCount(CounterType.CHARGE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Uses last-known choices and counters when the artifact leaves before resolution")
    void usesLastKnownSourceStateAfterArtifactLeaves() {
        Permanent replicator = addChosenReplicator(3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, replicator.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Riptide Replicator");
        harness.passBothPriorities();

        Permanent token = findToken(player1);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
    }

    private Permanent castReplicatorWithChoices(int xValue) {
        harness.setHand(player1, List.of(new RiptideReplicator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4 + xValue);

        harness.castArtifact(player1, 0, xValue);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GOBLIN");
        return findPermanent(player1, "Riptide Replicator");
    }

    private Permanent addChosenReplicator(int chargeCounters) {
        Permanent replicator = harness.addToBattlefieldAndReturn(player1, new RiptideReplicator());
        replicator.setChosenColor(CardColor.RED);
        replicator.setChosenSubtype(CardSubtype.GOBLIN);
        replicator.setCounterCount(CounterType.CHARGE, chargeCounters);
        return replicator;
    }

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
