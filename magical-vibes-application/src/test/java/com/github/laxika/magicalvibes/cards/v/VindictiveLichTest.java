package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({VindictiveLich.class, GrizzlyBears.class, Forest.class})
class VindictiveLichTest extends BaseCardTest {

    private static final String SACRIFICE_MODE = "Target opponent sacrifices a creature of their choice";
    private static final String DISCARD_MODE = "Target opponent discards two cards";
    private static final String LIFE_MODE = "Target opponent loses 5 life";

    @Test
    void sacrificeModeMakesTheTargetOpponentSacrificeACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addLich();

        killLich();
        harness.handleListChoice(player1, SACRIFICE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void discardModeMakesTheTargetOpponentDiscardTwoCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(first, second)));
        addLich();

        killLich();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void lifeModeMakesTheTargetOpponentLoseFiveLife() {
        addLich();

        killLich();
        harness.handleListChoice(player1, LIFE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void selectedModesTargetDifferentOpponents() {
        Player player3 = addOpponent("Charlie");
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addLich();

        killLich();
        harness.handleListChoice(player1, SACRIFICE_MODE);
        harness.handleListChoice(player1, LIFE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player3.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertLife(player2, 20);
        harness.assertLife(player3, 15);
    }

    private void addLich() {
        harness.addToBattlefield(player1, new VindictiveLich());
    }

    private void killLich() {
        Permanent lich = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VindictiveLich)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lich));
        harness.passBothPriorities();
    }

    private Player addOpponent(String name) {
        Player opponent = new Player(UUID.randomUUID(), name);
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(name);
        gd.playerIdToName.put(opponent.getId(), name);
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new com.github.laxika.magicalvibes.model.ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
    private static final String LIFE_LOSS_MODE = "Target opponent loses 5 life";

    @Test
    void sacrificeModeMakesTargetOpponentSacrificeACreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addLichAndTriggerDeath();

        chooseMode(SACRIFICE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void discardModeMakesTargetOpponentDiscardTwoCards() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        addLichAndTriggerDeath();

        chooseMode(DISCARD_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void lifeLossModeMakesTargetOpponentLoseFiveLife() {
        addLichAndTriggerDeath();

        chooseMode(LIFE_LOSS_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void modesCanOnlyTargetOpponents() {
        addLichAndTriggerDeath();
        harness.handleListChoice(player1, LIFE_LOSS_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addLichAndTriggerDeath() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new VindictiveLich());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lich));
        harness.passBothPriorities();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
    }

}
