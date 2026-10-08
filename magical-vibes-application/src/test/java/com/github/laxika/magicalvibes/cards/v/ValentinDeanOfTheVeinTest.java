package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FortifyingDraught;
import com.github.laxika.magicalvibes.cards.l.LisetteDeanOfTheRoot;
import com.github.laxika.magicalvibes.cards.l.LiesaForgottenArchangel;
import com.github.laxika.magicalvibes.cards.p.PestSummoning;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValentinDeanOfTheVein.class, LisetteDeanOfTheRoot.class, ScurridColony.class,
        Shock.class, FortifyingDraught.class})
class ValentinDeanOfTheVeinTest extends BaseCardTest {

    @Test
    void exilesOpponentNontokenCreatureAndCanCreatePest() {
        harness.addToBattlefield(player1, new ValentinDeanOfTheVein());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player2, "Scurrid Colony");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Scurrid Colony"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent pest = findPermanent(player1, "Pest");
        assertThat(pest.getCard().isToken()).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, pest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @CardUsed({PestSummoning.class})
    void doesNotReplaceTokenCreatureDying() {
        harness.addToBattlefield(player1, new ValentinDeanOfTheVein());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PestSummoning()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player2, 0, 0);
        Permanent token = findPermanent(player2, "Pest");
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
        assertThat(gd.exiledCards).isEmpty();
        harness.assertLife(player2, 21);
    }

    @Test
    void canDeclinePestPaymentWithoutUndoingExile() {
        harness.addToBattlefield(player1, new ValentinDeanOfTheVein());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(creature.getCard().getId()));
        assertThat(countPermanents(player1, "Pest")).isZero();
    }

    @Test
    void doesNotExileItsControllersCreature() {
        harness.addToBattlefield(player1, new ValentinDeanOfTheVein());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Scurrid Colony");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(countPermanents(player1, "Pest")).isZero();
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void abilityLossStopsTheExileReplacement() {
        Permanent valentin = harness.addToBattlefieldAndReturn(player1, new ValentinDeanOfTheVein());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, valentin.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Scurrid Colony");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @CardUsed({LiesaForgottenArchangel.class})
    void dyingCreaturesControllerChoosesBetweenCompetingExileReplacements() {
        harness.addToBattlefield(player1, new ValentinDeanOfTheVein());
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScurridColony());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertOnBattlefield(player2, "Scurrid Colony");
    }

    @Test
    void lisetteCanDeclinePayment() {
        Permanent lisette = harness.addToBattlefieldAndReturn(player1, new LisetteDeanOfTheRoot());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScurridColony());
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, lisette.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(lisette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lisette.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsLifeGainDoesNotTriggerLisette() {
        Permanent lisette = harness.addToBattlefieldAndReturn(player1, new LisetteDeanOfTheRoot());
        harness.setHand(player2, List.of(new FortifyingDraught()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, lisette.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 22);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lisette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void abilityLossStopsLisettesLifeGainTrigger() {
        Permanent lisette = harness.addToBattlefieldAndReturn(player1, new LisetteDeanOfTheRoot());
        harness.setHand(player1, List.of(new TurnToFrog(), new FortifyingDraught()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, lisette.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, lisette.getId());

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lisette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void lisettePutsCountersAndGrantsTrampleAfterLifeGain() {
        ValentinDeanOfTheVein card = new ValentinDeanOfTheVein();
        Permanent lisette = harness.addToBattlefieldAndReturn(player1, card);
        lisette.setCard(card.getBackFaceCard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ScurridColony());

        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.castAndResolveInstant(player1, 0, lisette.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(lisette.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lisette.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

}
