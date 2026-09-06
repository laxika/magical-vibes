package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RiptideReplicator.class)
class RiptideReplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X charge counters and stores the chosen color and type")
    void entersWithCountersAndChoices() {
        harness.setHand(player1, List.of(new RiptideReplicator()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GOBLIN");

        Permanent replicator = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
    }
}
