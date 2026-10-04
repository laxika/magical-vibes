package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.r.RecklessCohort;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrovetenderDruids.class, RecklessCohort.class, OranRiefInvoker.class, Conspiracy.class})
class GrovetenderDruidsTest extends BaseCardTest {

    @Test
    void ownAllyEntryMayCreatePlantToken() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromHand(player1, new GrovetenderDruids(), "{2}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertPlantToken();
    }

    @Test
    void anotherAllyEntryMayCreatePlantToken() {
        harness.addToBattlefield(player1, new GrovetenderDruids());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new RecklessCohort(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertPlantToken();
    }

    @Test
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrovetenderDruids());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromHand(player1, new OranRiefInvoker(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Plant")).isEmpty();
    }

    @Test
    void decliningPaymentDoesNotCreatePlantToken() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromHand(player1, new GrovetenderDruids(), "{2}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Plant")).isEmpty();
    }

    @Test
    void opponentsAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrovetenderDruids());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player2, new RecklessCohort(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Plant")).isEmpty();
        assertThat(findPermanents(player2, "Plant")).isEmpty();
    }

    @Test
    void acceptingWithoutManaDoesNotCreateToken() {
        harness.castFromHand(player1, new GrovetenderDruids(), "{2}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Plant")).isEmpty();
    }

    @Test
    void triggerStillCreatesTokenAfterDruidsLeaveBattlefield() {
        harness.addToBattlefield(player1, new GrovetenderDruids());
        Permanent druids = findPermanent(player1, "Grovetender Druids");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new RecklessCohort(), "{1}{R}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(druids);
        gd.playerGraveyards.get(player1.getId()).add(druids.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertPlantToken();
    }

    @Test
    @CardUsed({GrovetenderDruids.class, Conspiracy.class})
    void ownEntryTriggersEvenWhenCreatureTypesAreReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new GrovetenderDruids(), "{2}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Plant")).hasSize(1);
    }

    private void assertPlantToken() {
        assertThat(findPermanents(player1, "Plant")).hasSize(1);
        Permanent plant = findPermanent(player1, "Plant");
        assertThat(plant.getCard().isToken()).isTrue();
        assertThat(plant.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(plant.getCard().getPower()).isEqualTo(1);
        assertThat(plant.getCard().getToughness()).isEqualTo(1);
        assertThat(plant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(plant.getCard().getSubtypes()).containsExactly(CardSubtype.PLANT);
    }
}
