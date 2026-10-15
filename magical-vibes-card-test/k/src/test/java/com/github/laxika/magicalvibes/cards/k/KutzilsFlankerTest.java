package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KutzilsFlanker.class, Forest.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class KutzilsFlankerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself for each creature that left under its controller's control")
    void countsCreaturesThatLeftBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.setHand(player1, List.of(new KutzilsFlanker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0);
        resolveCreatureAndEtb();

        Permanent flanker = findFlanker();
        assertThat(flanker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains 2 life and scries 2")
    void gainsLifeAndScries() {
        Card topCard = new Forest();
        Card bottomCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLife(player1, 10);

        castFlanker(1);
        resolveCreatureAndEtb();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, bottomCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles the targeted player's graveyard")
    void exilesTargetPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new Shock()));

        castFlanker(2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chooses its entry-trigger mode after the creature resolves")
    void choosesModeWhenEntryTriggerIsPutOnStack() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new KutzilsFlanker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "You gain 2 life and scry 2");
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(findFlanker().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Adds no counters when no creatures have left this turn")
    void noDeparturesGiveNoCounters() {
        castFlanker(0);
        resolveCreatureAndEtb();

        assertThat(findFlanker().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count creatures that left under the opponent's control")
    void ignoresOpponentsDepartures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears"));

        castFlanker(0);
        resolveCreatureAndEtb();

        assertThat(findFlanker().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts creatures that leave while its counter trigger is on the stack")
    void countsDeparturesBeforeTriggerResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFlanker(0);
        harness.passBothPriorities();
        Permanent flanker = findFlanker();
        assertThat(flanker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(flanker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can exile its controller's graveyard without touching the opponent's")
    void canTargetOwnGraveyard() {
        Card ownCard = new Forest();
        Card opponentCard = new Shock();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castFlanker(2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castFlanker(int mode) {
        harness.setHand(player1, List.of(new KutzilsFlanker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, mode);
    }

    private Permanent findFlanker() {
        return findPermanent(player1, "Kutzil's Flanker");
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
