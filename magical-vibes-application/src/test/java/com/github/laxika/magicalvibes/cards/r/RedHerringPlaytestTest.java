package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedHerringPlaytest.class, GrizzlyBears.class})
class RedHerringPlaytestTest extends BaseCardTest {

    @Test
    void exchangesWithAControlledPermanentWithoutResettingItsState() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        UUID targetId = target.getId();
        Card exchangedCard = target.getOriginalCard();
        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();

        harness.activateHandAbility(player1, 0, null);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).contains(redHerring);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(target.getId()).isEqualTo(targetId);
        assertThat(target.getCard().getName()).isEqualTo("Red Herring");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(exchangedCard).doesNotContain(redHerring);
    }

    @Test
    void exchangesWithAControlledSpellAndTheNewSpellResolvesAsRedHerring() {
        GrizzlyBears spellCard = new GrizzlyBears();
        harness.castFromHand(player1, spellCard, "{1}{G}");
        StackEntry spell = gd.stack.getLast();
        UUID oldSpellId = spell.getTargetableId();

        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, oldSpellId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Red Herring");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addExchangeMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
