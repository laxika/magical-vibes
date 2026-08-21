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
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void lukkaEmblemMakesEnteringCreatureDealItsPower() {
        addReadyLukka(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(com.github.laxika.magicalvibes.model.PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private void addMilaAndPlaneswalker() {
        harness.addToBattlefield(player1, new MilaCraftyCompanion());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
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
