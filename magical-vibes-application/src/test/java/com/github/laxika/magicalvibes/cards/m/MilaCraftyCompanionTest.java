package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LukkaWaywardBonder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilaCraftyCompanion.class, Forest.class, GrizzlyBears.class, LukkaWaywardBonder.class,
        NicolBolasPlaneswalker.class, Shock.class})
class MilaCraftyCompanionTest extends BaseCardTest {

    @Test
    void milaAddsLoyaltyWhenOpponentAttacksControlledPlaneswalker() {
        addMilaAndPlaneswalker();
        Permanent attacker = addReadyCreature(player2);

        declareAttackers(attacker, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        Permanent planeswalker = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void milaDoesNotTriggerWhenOpponentAttacksPlayer() {
        addMilaAndPlaneswalker();
        Permanent attacker = addReadyCreature(player2);

        declareAttackers(attacker, player1.getId());
        harness.passBothPriorities();

        Permanent planeswalker = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void milaMayDrawWhenOwnPermanentBecomesOpponentSpellTarget() {
        harness.addToBattlefield(player1, new MilaCraftyCompanion());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof Forest);
    }

    @Test
    void lukkaDrawsTwoWhenCreatureCardWasDiscarded() {
        Permanent lukka = addReadyLukka(5);
        Card discardedCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof Forest);
    }

    @Test
    void lukkaReturnsCreatureWithHasteAndExilesItAtNextUpkeep() {
        Permanent lukka = addReadyLukka(5);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == creature)
                .findFirst().orElseThrow();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void lukkaEmblemMakesEnteringCreatureDealItsPower() {
        addReadyLukka(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(com.github.laxika.magicalvibes.model.PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void lukkaDrawsDuringTheSameResolutionAsDiscarding() {
        addReadyLukka(5);
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        List<Card> handAfterDiscardChoice = List.copyOf(gd.playerHands.get(player1.getId()));
        harness.passBothPriorities();

        assertThat(handAfterDiscardChoice).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void lukkaMayDeclineToDiscardWithoutDrawing() {
        Permanent lukka = addReadyLukka(5);
        Forest retained = new Forest();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void lukkaCannotDrawWithoutACardToDiscard() {
        addReadyLukka(5);
        harness.setHand(player1, List.of());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void milaMayDeclineDrawingWhenMilaItselfIsTargeted() {
        Permanent mila = harness.addToBattlefieldAndReturn(player1, new MilaCraftyCompanion());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mila.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mila);
    }

    @Test
    void milaAddsLoyaltyToUnattackedPlaneswalkersOnlyOnceForMultipleAttackers() {
        addMilaAndPlaneswalker();
        Permanent unattacked = harness.addToBattlefieldAndReturn(player1, new LukkaWaywardBonder());
        Permanent attacked = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent first = addCreatureReady(player2, new MilaCraftyCompanion());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        int firstIndex = gd.playerBattlefields.get(player2.getId()).indexOf(first);
        int secondIndex = gd.playerBattlefields.get(player2.getId()).indexOf(second);

        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd, player2, List.of(firstIndex, secondIndex),
                Map.of(firstIndex, attacked.getId(), secondIndex, attacked.getId())));
        harness.passBothPriorities();

        assertThat(attacked.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(unattacked.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void lukkaReturnedCreatureSurvivesOpponentsUpkeep() {
        addReadyLukka(5);
        MilaCraftyCompanion creature = new MilaCraftyCompanion();
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Mila, Crafty Companion");

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == creature
                && entry.ownerId().equals(player1.getId()));
    }

    @Test
    void canCastMilaFaceAndItsTargetingAbilityWorks() {
        harness.setHand(player1, List.of(new MilaCraftyCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        Permanent mila = findPermanent(player1, "Mila, Crafty Companion");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mila.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void canCastLukkaFaceAndActivateItsLoyaltyAbility() {
        harness.setHand(player1, List.of(new MilaCraftyCompanion()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent lukka = findPermanent(player1, "Lukka, Wayward Bonder");
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void milaDoesNotDrawForItsControllersSpell() {
        Permanent mila = harness.addToBattlefieldAndReturn(player1, new MilaCraftyCompanion());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mila.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lukkaEmblemUsesCreaturesPowerWhenDamageResolves() {
        addReadyLukka(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new MilaCraftyCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        Permanent mila = findPermanent(player1, "Mila, Crafty Companion");
        mila.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int lifeBefore = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    private void addMilaAndPlaneswalker() {
        harness.addToBattlefield(player1, new MilaCraftyCompanion());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addReadyLukka(int loyalty) {
        Permanent lukka = harness.addToBattlefieldAndReturn(player1, new LukkaWaywardBonder());
        lukka.setCounterCount(CounterType.LOYALTY, loyalty);
        lukka.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return lukka;
    }

    private void declareAttackers(Permanent attacker, java.util.UUID targetId) {
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        harness.inMutationScope(() -> harness.getCombatAttackService().declareAttackers(
                gd, player2, List.of(attackerIndex), Map.of(attackerIndex, targetId)));
    }
}
