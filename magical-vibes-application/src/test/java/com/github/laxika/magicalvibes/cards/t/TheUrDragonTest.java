package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheUrDragon.class, DragonWhelp.class, GrizzlyBears.class})
class TheUrDragonTest extends BaseCardTest {

    @Test
    void reducesOtherDragonSpellsFromTheBattlefield() {
        addCreatureReady(player1, new TheUrDragon());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesOtherDragonSpellsFromTheCommandZone() {
        gd.playerCommandZones.get(player1.getId()).add(new TheUrDragon());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void drawsForEachAttackingDragonAndMayPutAPermanentFromHand() {
        addCreatureReady(player1, new TheUrDragon());
        addCreatureReady(player1, new DragonWhelp());
        addCreatureReady(player1, new DragonWhelp());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void doesNotTriggerForNonDragonAttackers() {
        addCreatureReady(player1, new TheUrDragon());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
