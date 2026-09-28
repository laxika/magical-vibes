package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanonTheReturnersLeader.class, Forest.class, GrizzlyBears.class, Shock.class, TormentingVoice.class})
class BanonTheReturnersLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature discarded from hand this turn")
    void castsCreaturePutIntoGraveyardFromNonBattlefield() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Card tormentingVoice = new TormentingVoice();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(tormentingVoice, creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        int creatureIndex = gd.playerGraveyards.get(player1.getId()).indexOf(creature);
        assertThat(creatureIndex).isGreaterThanOrEqualTo(0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.castFromGraveyard(player1, creatureIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("does not cast a creature put into the graveyard from the battlefield")
    void rejectsCreaturePutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature.getCard())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("may pay and discard to draw when attacking")
    void attackingMayPayToDiscardAndDraw() {
        addCreatureReady(player1, new BanonTheReturnersLeader());
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_ATTACKERS));
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }
}
