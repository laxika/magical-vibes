package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarsetTranscendent.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class NarsetTranscendentTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a noncreature, nonland top card into hand when accepted")
    void plusOnePutsMatchingCardIntoHand() {
        addReadyNarset(player1, 3);
        harness.setLibrary(player1, deckOf(new LightningBolt(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lightning Bolt");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("+1 does not offer a land from the top of the library")
    void plusOneLeavesLandOnTop() {
        addReadyNarset(player1, 3);
        harness.setLibrary(player1, deckOf(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("-2 grants rebound to the next instant or sorcery cast from hand")
    void minusTwoGrantsReboundToNextHandInstantOrSorcery() {
        Permanent narset = addReadyNarset(player1, 3);
        LightningBolt firstBolt = new LightningBolt();
        LightningBolt secondBolt = new LightningBolt();
        harness.setHand(player1, List.of(firstBolt, secondBolt));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(narset);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(firstBolt.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(secondBolt.getId()));
    }

    @Test
    @DisplayName("-9 prevents opponents from casting noncreature spells but allows creature spells")
    void minusNineCreatesOpponentNoncreatureRestrictionEmblem() {
        addReadyNarset(player1, 9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadyNarset(Player player, int loyalty) {
        Permanent perm = new Permanent(new NarsetTranscendent());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
