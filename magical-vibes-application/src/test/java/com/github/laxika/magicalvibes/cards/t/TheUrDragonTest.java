package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({TheUrDragon.class, DragonWhelp.class, GrizzlyBears.class, Forest.class})
class TheUrDragonTest extends BaseCardTest {

    @Test
    void reducesOtherDragonSpellsFromTheBattlefield() {
        addCreatureReady(player1, new TheUrDragon());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesOtherDragonSpellsFromTheCommandZone() {
        gd.playerCommandZones.get(player1.getId()).add(new TheUrDragon());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 2);
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
    @Test
    void commandZoneEminenceReducesOtherDragonSpells() {
        addToCommandZone(player1, new TheUrDragon());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void attackDrawsForOnlyTheAttackingDragons() {
        addCreatureReady(player1, new TheUrDragon());
        addCreatureReady(player1, new DragonWhelp());
        addCreatureReady(player1, new GrizzlyBears());
        Card firstDraw = new Forest();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdDraw);
    }

    @Test
    void attackMayPutAPermanentFromHandOntoTheBattlefield() {
        addCreatureReady(player1, new TheUrDragon());
        addCreatureReady(player1, new DragonWhelp());
        Card libraryCard = new Forest();
        Card secondDraw = new Forest();
        Card permanentToPut = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard, secondDraw));
        harness.setHand(player1, List.of(permanentToPut));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard, secondDraw);
    }

    @Test
    void doesNotReduceItsOwnCommandZoneCastingCost() {
        TheUrDragon commander = new TheUrDragon();
        addToCommandZone(player1, commander);
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.castCommander(gd, player1, commander.getId(),
                () -> harness.castCreature(player1, 0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
    }

    @Test
    void doesNotReduceNonDragonSpells() {
        addToCommandZone(player1, new TheUrDragon());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceDragonSpellsWhileInHand() {
        harness.setHand(player1, List.of(new DragonWhelp(), new TheUrDragon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void commandZoneDoesNotGrantTheAttackTrigger() {
        addToCommandZone(player1, new TheUrDragon());
        addCreatureReady(player1, new DragonWhelp());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void mayPutTheNewlyDrawnLandOntoTheBattlefield() {
        addCreatureReady(player1, new TheUrDragon());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }

}
