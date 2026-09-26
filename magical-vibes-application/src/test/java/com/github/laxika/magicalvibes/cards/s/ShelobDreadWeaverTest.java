package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShelobDreadWeaver.class, GrizzlyBears.class, Murder.class, Forest.class})
class ShelobDreadWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nontoken creature an opponent controls when it dies")
    void exilesOpponentCreatureThatDies() {
        Permanent shelob = harness.addToBattlefieldAndReturn(player1, new ShelobDreadWeaver());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(shelob.getId()))
                .extracting(Card::getId).containsExactly(target.getCard().getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puts a chosen exiled creature into its owner's graveyard, then adds counters and draws")
    void paysCreatureFromExileForCountersAndCardDraw() {
        Permanent shelob = harness.addToBattlefieldAndReturn(player1, new ShelobDreadWeaver());
        Card creature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card noncreature = new Forest();
        gd.addToExile(player2.getId(), creature, shelob.getId());
        gd.addToExile(player2.getId(), secondCreature, shelob.getId());
        gd.addToExile(player2.getId(), noncreature, shelob.getId());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), secondCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(shelob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(gd.getCardsExiledByPermanent(shelob.getId())).containsExactly(secondCreature, noncreature);
    }

    @Test
    @DisplayName("Returns a targeted creature with mana value X tapped under its controller's control")
    void returnsTargetedCreatureWithManaValueX() {
        Permanent shelob = harness.addToBattlefieldAndReturn(player1, new ShelobDreadWeaver());
        Card creature = new GrizzlyBears();
        gd.addToExile(player2.getId(), creature, shelob.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 2, creature.getId(), Zone.EXILE);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(shelob.getId())).isEmpty();
    }
}
