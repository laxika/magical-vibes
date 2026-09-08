package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoldIntoAetherTest extends BaseCardTest {

    @Test
    void countersSpellAndOffersItsControllerACreatureFromHand() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears creature = new GrizzlyBears();

        harness.setHand(player1, List.of(target, creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FoldIntoAether()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningTheOptionalCreatureLeavesItInTheTargetControllersHand() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears creature = new GrizzlyBears();

        harness.setHand(player1, List.of(target, creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FoldIntoAether()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void doesNotOfferTheCreatureWhenTheTargetSpellCannotBeCountered() {
        CarnageTyrant target = new CarnageTyrant();
        GrizzlyBears creature = new GrizzlyBears();

        harness.setHand(player1, List.of(target, creature));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new FoldIntoAether()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Carnage Tyrant");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }
}
