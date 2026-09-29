package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesdemonaFreedomsEdge.class, Bonesplitter.class, GrizzlyBears.class,
        LavaSpike.class, Ornithopter.class, Shock.class, ThunderingGiant.class})
class DesdemonaFreedomsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Attack targets a qualifying creature card in your graveyard")
    void attackTargetsQualifyingCreatureCard() {
        Card artifactCreature = new Ornithopter();
        Card lowManaCreature = new GrizzlyBears();
        Card expensiveCreature = new ThunderingGiant();
        Card noncreatureArtifact = new Bonesplitter();
        harness.setGraveyard(player1, List.of(
                artifactCreature, lowManaCreature, expensiveCreature, noncreatureArtifact));
        addReadyDesdemona();

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                artifactCreature.getId(), lowManaCreature.getId());
    }

    @Test
    @DisplayName("The granted escape permission exiles two other graveyard cards when cast")
    void grantsEscapeAndExilesTwoOtherCards() {
        Card creature = new GrizzlyBears();
        Card firstExiledCard = new Shock();
        Card secondExiledCard = new LavaSpike();
        harness.setGraveyard(player1, List.of(creature, firstExiledCard, secondExiledCard));
        addReadyDesdemona();

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        var permission = gd.graveyardCardCastPermissionsUntilEndOfTurn.get(creature.getId());
        assertThat(permission).isNotNull();
        assertThat(permission.additionalGraveyardExileCount()).isEqualTo(2);
        assertThat(permission.escape()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstExiledCard.getId(), secondExiledCard.getId());
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).doesNotContainKey(creature.getId());
    }

    private Permanent addReadyDesdemona() {
        return addCreatureReady(player1, new DesdemonaFreedomsEdge());
    }
}
