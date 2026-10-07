package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormentedThoughts.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, Peek.class, YokedOx.class})
class TormentedThoughtsTest extends BaseCardTest {

    private void prepareCaster() {
        harness.setHand(player1, List.of(new TormentedThoughts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Target discards cards equal to the sacrificed creature's power")
    void targetDiscardsEqualToSacrificedPower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Sacrificing a 1-power creature discards only one card")
    void onePowerCreatureDiscardsOne() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificed creature's power includes +1/+1 counters")
    void powerIncludesCounters() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // becomes 4/4

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest(), new Forest(), new Forest())));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Target with an empty hand discards nothing")
    void emptyHandDiscardsNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player2, new ArrayList<>(List.of()));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        prepareCaster();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareCaster();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    void zeroPowerCreatureIsStillSacrificedButNoCardsAreDiscarded() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new YokedOx());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.assertNotOnBattlefield(player1, "Yoked Ox");
        harness.assertInGraveyard(player1, "Yoked Ox");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Tormented Thoughts");
    }

    @Test
    void targetDiscardsEntireHandWhenPowerExceedsHandSize() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player2, List.of(new Forest(), new Peek()));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    void casterCanTargetThemselves() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCaster();
        harness.setHand(player1, List.of(new TormentedThoughts(), new Forest(), new Forest(), new Peek()));
        harness.setHand(player2, List.of(new Forest()));

        harness.castSorceryWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void negativePowerDoesNotCauseAnyDiscard() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new YokedOx());
        sacrifice.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player2, List.of(new Forest()));
        prepareCaster();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yoked Ox");
        harness.assertInGraveyard(player1, "Tormented Thoughts");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotSacrificeANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareCaster();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Tormented Thoughts");
    }
}
