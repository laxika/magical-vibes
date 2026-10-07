package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpeedbroodStalker.class, GrizzlyBears.class, HillGiant.class, LilianaVess.class})
@DisplayName("Speedbrood Stalker")
class SpeedbroodStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Secretly chosen permanent is sacrificed after the opponent's chosen sacrifice")
    void secretlyChosenPermanentIsSacrificedAfterOpponentsChoice() {
        Permanent secretlyChosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentChoice = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent remains = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.context())
                .isInstanceOf(MultiPermanentChoiceContext.TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(secretlyChosen.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentChoice.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(remains.getId());
    }

    @Test
    @DisplayName("The enter ability cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SpeedbroodStalker()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Choosing the secretly chosen creature for the first sacrifice leaves the other creature")
    void opponentCanSacrificeTheSecretlyChosenCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remains = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).containsExactly(remains.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("An opponent with just one creature sacrifices it only once")
    void singleCreatureIsSacrificedOnlyOnce() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent with no eligible permanent requires no choice")
    void emptyOpponentBattlefieldRequiresNoChoice() {
        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Speedbrood Stalker");
    }

    @Test
    @DisplayName("A planeswalker can be secretly chosen and sacrificed after a creature")
    void secretlyChosenPlaneswalkerIsSacrificed() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        chosen.setCounterCount(CounterType.LOYALTY, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Liliana Vess");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The opponent can choose a planeswalker for the first sacrifice")
    void opponentCanSacrificePlaneswalkerFirst() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Liliana Vess");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castSpeedbroodStalker(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SpeedbroodStalker()));
        addMana();
        harness.castCreature(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
