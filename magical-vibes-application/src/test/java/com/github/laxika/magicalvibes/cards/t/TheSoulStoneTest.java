package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.k.KravensCats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSoulStone.class, KravensCats.class, EnsoulArtifact.class})
class TheSoulStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one black mana")
    void tapsForBlackMana() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not return a creature before it is harnessed")
    void doesNotReturnCreatureBeforeHarnessed() {
        harness.addToBattlefield(player1, new TheSoulStone());
        harness.setGraveyard(player1, List.of(new KravensCats()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kraven's Cats");
    }

    @Test
    @DisplayName("Harnessing returns a target creature from the graveyard at upkeep")
    void harnessingReturnsCreatureAtUpkeep() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());
        Permanent creatureToExile = addCreatureReady(player1, new KravensCats());
        addCreatureReady(player1, new KravensCats());
        KravensCats creatureToReturn = new KravensCats();
        harness.setGraveyard(player1, List.of(creatureToReturn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureToExile.getId());
        assertThat(stone.isHarnessed()).isFalse();
        assertThat(stone.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureToExile.getCard());
        harness.passBothPriorities();

        assertThat(stone.isHarnessed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creatureToExile);

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creatureToReturn.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureToReturn.getId()));
        harness.assertNotInGraveyard(player1, "Kraven's Cats");
    }

    @Test
    @DisplayName("A harnessed Stone does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());
        stone.setHarnessed(true);
        harness.setGraveyard(player1, List.of(new KravensCats()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Kraven's Cats");
    }

    @Test
    @DisplayName("Upkeep must target a creature in its controller's graveyard")
    void upkeepTargetsOnlyOwnCreatureCards() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());
        stone.setHarnessed(true);
        KravensCats creature = new KravensCats();
        harness.setGraveyard(player1, List.of(creature, new TheSoulStone()));
        harness.setGraveyard(player2, List.of(new KravensCats()));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(creature);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kraven's Cats");
        harness.assertInGraveyard(player2, "Kraven's Cats");
        harness.assertInGraveyard(player1, "The Soul Stone");
    }

    @Test
    @DisplayName("Upkeep does not put a trigger on the stack without a legal target")
    void noTriggerWithoutLegalGraveyardTarget() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());
        stone.setHarnessed(true);
        harness.setGraveyard(player1, List.of(new TheSoulStone()));
        harness.setGraveyard(player2, List.of(new KravensCats()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Harnessing cannot exile an opponent's creature")
    void cannotHarnessUsingOpponentsCreature() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheSoulStone());
        harness.addToBattlefield(player2, new KravensCats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stone.isHarnessed()).isFalse();
        harness.assertOnBattlefield(player2, "Kraven's Cats");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated Stone can exile itself to pay its harness cost")
    void animatedStoneCanExileItselfToHarness() {
        Permanent stone = addCreatureReady(player1, new TheSoulStone());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, stone.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Soul Stone");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(stone.getCard());
        assertThat(gd.stack).isEmpty();
    }
}
