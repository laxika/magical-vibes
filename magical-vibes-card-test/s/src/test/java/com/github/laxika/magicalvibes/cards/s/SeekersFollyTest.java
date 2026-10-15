package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekersFolly.class, BearCub.class, LlanowarElves.class})
class SeekersFollyTest extends BaseCardTest {

    @Test
    void opponentDiscardModeDiscardsTwoCards() {
        harness.setHand(player2, List.of(new BearCub(), new LlanowarElves()));
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void discardModeCannotTargetController() {
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCreatureModeDebuffsOnlyOpponentsUntilEndOfTurn() {
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player2, new BearCub());
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectiveToughness()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void opponentChoosesWhichTwoCardsToDiscard() {
        BearCub keptCard = new BearCub();
        harness.setHand(player2, List.of(keptCard, new LlanowarElves(), new BearCub()));
        harness.addToBattlefield(player2, new BearCub());
        harness.setHand(player1, List.of(new SeekersFolly(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void discardModeDiscardsOnlyAvailableCard() {
        harness.setHand(player2, List.of(new BearCub()));
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Bear Cub");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardModeResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Seeker's Folly");
    }

    @Test
    void creatureModeKillsOneToughnessCreaturesWithoutDiscarding() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player2, List.of(new BearCub(), new LlanowarElves()));
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureModeDoesNotAffectCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player2, new BearCub());
        harness.setHand(player1, List.of(new SeekersFolly()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, (UUID) null);
        harness.passBothPriorities();
        var laterCreature = harness.enterBattlefieldAndReturn(player2, new BearCub());

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectiveToughness()).isEqualTo(1);
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }
}
