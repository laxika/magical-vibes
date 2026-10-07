package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Berserk;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellQueller.class, GrizzlyBears.class, SerraAngel.class, LightningBolt.class,
        HillGiant.class, Berserk.class, Counterspell.class})
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
        harness.castAndResolveInstant(player2, 0, quellerId);
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

    @Test
    @DisplayName("A spell with mana value exactly four can be exiled")
    void exilesSpellAtManaValueBoundary() {
        HillGiant giant = new HillGiant();
        quellOpponentSpell(giant, ManaColor.RED, 4);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves leaves the spell exiled")
    void leavingBeforeEnterTriggerResolvesExilesPermanently() {
        GrizzlyBears bears = new GrizzlyBears();
        quellOpponentSpell(bears, ManaColor.GREEN, 2);
        harness.handlePermanentChosen(player1, bears.getId());

        boltTheQueller();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Free casting still respects Berserk's explicit timing restriction")
    void cannotFreeCastBerserkAfterCombatDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        Berserk berserk = new Berserk();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(berserk));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, bearsId);
        harness.castFromHand(player1, new SpellQueller(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, berserk.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Spell Queller"));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(berserk.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(berserk.getId()));
    }

    @Test
    @DisplayName("An exiled Counterspell cannot be cast when no spell remains to target")
    void cannotFreeCastCounterspellWithoutLegalTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.castFromHand(player1, new SpellQueller(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, counterspell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        boltTheQueller();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(counterspell.getId()));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(counterspell.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
