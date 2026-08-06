package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpellQuellerTest extends BaseCardTest {

    /**
     * player2 casts {@code spell}, player1 flashes in Spell Queller in response and exiles it with
     * the enter-the-battlefield trigger.
     */
    private void quellOpponentSpell(com.github.laxika.magicalvibes.model.Card spell, ManaColor spellColor, int spellCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, spellColor, spellCost);
        harness.setHand(player1, List.of(new SpellQueller()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0); // Flash
        harness.passBothPriorities();     // Queller resolves and enters
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** Kills the Queller with a Lightning Bolt cast by player2. */
    private void boltTheQueller() {
        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID quellerId = harness.getPermanentId(player1, "Spell Queller");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, quellerId);
        harness.passBothPriorities(); // resolve Bolt -> Queller dies, its leaves trigger goes on the stack
    }

    @Test
    @DisplayName("ETB exiles a target spell with mana value 4 or less")
    void etbExilesSmallSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        quellOpponentSpell(bears, ManaColor.GREEN, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("A spell with mana value 5 is not a legal target")
    void cannotTargetSpellAboveManaValueFour() {
        SerraAngel angel = new SerraAngel();
        quellOpponentSpell(angel, ManaColor.WHITE, 5);

        // No legal target — the trigger is skipped and nothing is chosen.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities(); // resolve Serra Angel
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("When the Queller leaves, the exiled card's owner may cast it for free")
    void ownerMayCastExiledCardWhenQuellerLeaves() {
        GrizzlyBears bears = new GrizzlyBears();
        quellOpponentSpell(bears, ManaColor.GREEN, 2);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        boltTheQueller();
        harness.passBothPriorities(); // resolve the leaves trigger -> free-cast offer

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        // The offer belongs to the exiled card's owner, not the Queller's controller.
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities(); // resolve the free-cast Grizzly Bears spell

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the free cast leaves the card exiled")
    void decliningFreeCastLeavesCardExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        quellOpponentSpell(bears, ManaColor.GREEN, 2);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        boltTheQueller();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }
}
