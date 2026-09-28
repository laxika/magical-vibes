package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolgariGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necroplasm.class, GolgariGuildmage.class, BorosRecruit.class, Forest.class})
class NecroplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Its upkeep trigger puts a +1/+1 counter on it")
    void putsCounterOnItselfAtUpkeep() {
        Permanent necroplasm = addNecroplasm(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(necroplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its upkeep trigger does not fire on an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent necroplasm = addNecroplasm(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(necroplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Its end-step trigger destroys creatures with matching mana value")
    void destroysCreaturesWithMatchingManaValue() {
        Permanent necroplasm = addNecroplasm(player1);
        necroplasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownGuildmage = harness.addToBattlefieldAndReturn(player1, new GolgariGuildmage());
        Permanent opponentGuildmage = harness.addToBattlefieldAndReturn(player2, new GolgariGuildmage());
        Permanent opponentRecruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        triggerEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownGuildmage);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentGuildmage)
                .contains(opponentRecruit);
        harness.assertInGraveyard(player1, "Golgari Guildmage");
        harness.assertInGraveyard(player2, "Golgari Guildmage");
    }

    @Test
    @DisplayName("The upkeep counter is counted by that turn's end-step trigger")
    void upkeepCounterIsCountedAtEndStep() {
        Permanent necroplasm = addNecroplasm(player1);
        necroplasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent guildmage = harness.addToBattlefieldAndReturn(player2, new GolgariGuildmage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        triggerEndStep(player1);

        assertThat(necroplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guildmage);
    }

    @Test
    @DisplayName("Its end-step trigger ignores noncreatures with matching mana value")
    void ignoresNoncreaturesWithMatchingManaValue() {
        addNecroplasm(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        triggerEndStep(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Its end-step trigger does not fire on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        addNecroplasm(player1).setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent guildmage = harness.addToBattlefieldAndReturn(player2, new GolgariGuildmage());

        triggerEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guildmage);
    }

    @Test
    @DisplayName("Dredge 2 returns Necroplasm instead of drawing")
    void dredgesInsteadOfDrawing() {
        Necroplasm necroplasm = new Necroplasm();
        List<Card> milled = List.of(new Forest(), new GolgariGuildmage());
        harness.setGraveyard(player1, List.of(necroplasm));
        harness.setLibrary(player1, milled);

        resolveDraw();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(necroplasm);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
    }

    @Test
    @DisplayName("Declining dredge draws normally")
    void declinesDredge() {
        Necroplasm necroplasm = new Necroplasm();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(necroplasm));
        harness.setLibrary(player1, List.of(topCard, new GolgariGuildmage()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(necroplasm);
    }

    private Permanent addNecroplasm(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Necroplasm());
    }

    private void triggerEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
