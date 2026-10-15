package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.e.ElderMastery;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.r.RottingRats;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Traumatize;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Progenitus.class, CruelEdict.class, Shock.class, RottingRats.class,
        Traumatize.class, VolcanicFallout.class, WrathOfGod.class,
        LeylineOfTheVoid.class, KalitasTraitorOfGhet.class, BoneSaw.class,
        ElderMastery.class, Cancel.class})
class ProgenitusTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Shock cannot target Progenitus (protection from everything)")
    void protectionFromEverythingRejectsTargetedSpell() {
        Permanent progenitus = harness.addToBattlefieldAndReturn(player2, new Progenitus());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, progenitus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No creature can block Progenitus (protection from everything)")
    void protectionFromEverythingCannotBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new Progenitus());
        attacker.setAttacking(true);

        addCreatureReady(player2, createCreature("Hill Giant", 3, 3));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Progenitus takes no combat damage while blocking (protection from everything)")
    void protectionFromEverythingPreventsCombatDamage() {
        // A 12/12 attacker would normally kill the 10/10 Progenitus, but all damage is prevented.
        Permanent attacker = addCreatureReady(player1, createCreature("Colossus", 12, 12));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Progenitus());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Progenitus survives the 12 damage (prevented) and deals 10 back (attacker survives at 12/2).
        harness.assertOnBattlefield(player2, "Progenitus");
        harness.assertOnBattlefield(player1, "Colossus");
    }

    @Test
    @DisplayName("When sacrificed, Progenitus is shuffled into its owner's library instead of the graveyard")
    void shuffleReplacementOnSacrifice() {
        harness.addToBattlefield(player2, new Progenitus());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Progenitus");
        harness.assertNotInGraveyard(player2, "Progenitus");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Progenitus"));
    }

    @Test
    @DisplayName("Progenitus resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new Progenitus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Progenitus");
    }

    @Test
    void preventsUntargetedDamage() {
        Permanent progenitus = harness.addToBattlefieldAndReturn(player2, new Progenitus());
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(progenitus.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Progenitus");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotBeTargetedByAnAura() {
        Permanent progenitus = harness.addToBattlefieldAndReturn(player1, new Progenitus());
        harness.setHand(player1, List.of(new ElderMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, progenitus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeEquippedByColorlessEquipment() {
        harness.addToBattlefield(player1, new BoneSaw());
        Permanent progenitus = harness.addToBattlefieldAndReturn(player1, new Progenitus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, progenitus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalAttachmentsAreRemovedWithoutTargeting() {
        Permanent progenitus = harness.addToBattlefieldAndReturn(player1, new Progenitus());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElderMastery());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        aura.setAttachedTo(progenitus.getId());
        equipment.setAttachedTo(progenitus.getId());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Elder Mastery");
        harness.assertNotOnBattlefield(player1, "Elder Mastery");
        harness.assertOnBattlefield(player1, "Bone Saw");
        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Progenitus");
    }

    @Test
    void untargetedDestructionShufflesIntoLibrary() {
        Progenitus progenitus = new Progenitus();
        harness.addToBattlefield(player2, progenitus);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);

        harness.assertNotOnBattlefield(player2, "Progenitus");
        harness.assertNotInGraveyard(player2, "Progenitus");
        assertThat(gd.playerDecks.get(player2.getId())).contains(progenitus);
        assertThat(gd.creatureDeathCountThisTurn.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void discardedProgenitusShufflesIntoLibrary() {
        Progenitus progenitus = new Progenitus();
        harness.setHand(player1, List.of(new RottingRats()));
        harness.setHand(player2, List.of(progenitus));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertNotInHand(player2, "Progenitus");
        harness.assertNotInGraveyard(player2, "Progenitus");
        assertThat(gd.playerDecks.get(player2.getId())).contains(progenitus);
    }

    @Test
    void milledProgenitusShufflesOnlyItselfIntoLibrary() {
        Progenitus progenitus = new Progenitus();
        RottingRats milled = new RottingRats();
        harness.setLibrary(player2, List.of(progenitus, milled, new BoneSaw(), new ElderMastery()));
        harness.setHand(player1, List.of(new Traumatize()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotInGraveyard(player2, "Progenitus");
        harness.assertInGraveyard(player2, "Rotting Rats");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3).contains(progenitus);
    }

    @Test
    void protectionDoesNotPreventCounteringAndCounteredCardShuffles() {
        Progenitus progenitus = new Progenitus();
        harness.setHand(player1, List.of(progenitus));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 2);
        }
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, progenitus.getId());

        harness.assertNotOnBattlefield(player1, "Progenitus");
        harness.assertNotInGraveyard(player1, "Progenitus");
        assertThat(gd.playerDecks.get(player1.getId())).contains(progenitus);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerChoosesReplacementForStolenProgenitus() {
        Progenitus progenitus = new Progenitus();
        progenitus.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, progenitus);
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "SHUFFLE");
        assertThat(gd.playerDecks.get(player1.getId())).contains(progenitus);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(progenitus);
    }

    @Test
    void canChooseShuffleInsteadOfKalitasExileReplacement() {
        Progenitus progenitus = new Progenitus();
        harness.addToBattlefield(player2, progenitus);
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "SHUFFLE");
        assertThat(gd.playerDecks.get(player2.getId())).contains(progenitus);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(progenitus);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
