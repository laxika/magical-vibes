package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AminatouTheFateshifter.class, Forest.class, GrizzlyBears.class})
class AminatouTheFateshifterTest extends BaseCardTest {

    @Test
    void drawsThenPutsAChosenCardOnTopOfTheLibrary() {
        Permanent aminatou = addAminatou(3);
        Card drawn = new Forest();
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(aminatou.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void flickersAnotherPermanentYouOwnUnderYourControl() {
        Permanent aminatou = addAminatou(3);
        Card ownedCard = new GrizzlyBears();
        ownedCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, ownedCard);
        gd.stolenCreatures.put(target.getId(), player1.getId());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aminatou, returned);
        assertThat(aminatou.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void cannotTargetAPermanentYouDoNotOwn() {
        addAminatou(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent you own");
    }

    @Test
    void ultimateRotatesNonlandsAndLeavesAminatouAndLandsAlone() {
        Permanent aminatou = addAminatou(7);
        Permanent player1Creature = addReady(player1, new GrizzlyBears());
        Permanent player2Creature = addReady(player2, new GrizzlyBears());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aminatou, player2Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player1Creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Land);
        assertThat(aminatou.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private Permanent addAminatou(int loyalty) {
        return addReady(player1, new AminatouTheFateshifter(), loyalty);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        return addReady(player, card, 0);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card, int loyalty) {
        Permanent permanent = new Permanent(card);
        if (loyalty > 0) {
            permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        }
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
