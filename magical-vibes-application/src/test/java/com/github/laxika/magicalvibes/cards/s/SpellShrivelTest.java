package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Blisterpod;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellShrivel.class, GrizzlyBears.class, Blisterpod.class})
class SpellShrivelTest extends BaseCardTest {

    @Test
    void countersAndExilesSpellWhenItsControllerCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellShrivel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void spellResolvesWhenItsControllerPaysFourMana() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new SpellShrivel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @CardUsed({SpellShrivel.class, Blisterpod.class})
    void countersAndExilesWhenOnlyThreeManaRemain() {
        Blisterpod spell = new Blisterpod();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new SpellShrivel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Blisterpod");
        harness.assertNotOnBattlefield(player1, "Blisterpod");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({SpellShrivel.class, Blisterpod.class})
    void decliningPaymentExilesSpellUnderItsOwnerEvenWhenAnotherPlayerControlsIt() {
        Blisterpod spell = new Blisterpod();
        spell.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new SpellShrivel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        gd.stack.getFirst().setOwnerIdOverride(player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        harness.assertNotOnBattlefield(player1, "Blisterpod");
        harness.assertNotInGraveyard(player1, "Blisterpod");
        harness.assertNotInGraveyard(player2, "Blisterpod");
    }

    @Test
    @CardUsed({SpellShrivel.class, Blisterpod.class})
    void canCounterAndExileAnotherInstant() {
        Blisterpod creature = new Blisterpod();
        SpellShrivel firstCounter = new SpellShrivel();
        harness.setHand(player1, List.of(creature, new SpellShrivel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(firstCounter));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, firstCounter.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(firstCounter);
        harness.assertNotInGraveyard(player2, "Spell Shrivel");
        harness.assertOnBattlefield(player1, "Blisterpod");
    }
}
