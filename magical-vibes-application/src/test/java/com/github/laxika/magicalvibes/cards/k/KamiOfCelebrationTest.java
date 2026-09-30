package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({KamiOfCelebration.class, DrakeHatchling.class, GrizzlyBears.class})
class KamiOfCelebrationTest extends BaseCardTest {

    @Test
    @DisplayName("A modified creature attacking exiles the top card and permits playing it")
    void modifiedCreatureAttackExilesTopCard() {
        addCreatureReady(player1, new KamiOfCelebration());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Casting a spell from exile puts a counter on a creature you control")
    void castingFromExilePutsCounterOnTargetCreature() {
        addCreatureReady(player1, new KamiOfCelebration());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, topCard.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unmodified creature attacking does not exile a card")
    void unmodifiedCreatureAttackDoesNotTrigger() {
        addCreatureReady(player1, new KamiOfCelebration());
        addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

}
