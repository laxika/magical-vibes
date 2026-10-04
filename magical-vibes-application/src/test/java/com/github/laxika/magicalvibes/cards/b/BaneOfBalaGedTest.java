package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.n.NissaVoiceOfZendikar;
import com.github.laxika.magicalvibes.cards.r.RealmbreakersGrasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfBalaGed.class, Forest.class, NissaVoiceOfZendikar.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class, RealmbreakersGrasp.class})
class BaneOfBalaGedTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player chooses two permanents to exile when Bane of Bala Ged attacks")
    void defendingPlayerChoosesTwoPermanentsToExile() {
        addCreatureReady(player1, new BaneOfBalaGed());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent thirdForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstForest.getId(), secondForest.getId(), thirdForest.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DefendingPlayerChoosesPermanentsToExile.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(firstForest.getId(), thirdForest.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstForest, thirdForest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiles all available permanents when the defending player controls fewer than two")
    void exilesAllAvailablePermanentsWhenFewerThanTwoExist() {
        addCreatureReady(player1, new BaneOfBalaGed());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exilesExactlyTwoAvailablePermanentsWithoutPrompting() {
        Permanent attacker = addCreatureReady(player1, new BaneOfBalaGed());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithoutPermanentsToExile() {
        Permanent attacker = addCreatureReady(player1, new BaneOfBalaGed());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void triggerStillExilesPermanentsAfterAttackerLeaves() {
        Permanent attacker = addCreatureReady(player1, new BaneOfBalaGed());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, attacker));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void defendingPlayerMayExileTheAttackedPlaneswalker() {
        addCreatureReady(player1, new BaneOfBalaGed());
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaVoiceOfZendikar());
        nissa.setCounterCount(CounterType.LOYALTY, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackAt(nissa);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(nissa.getId(), first.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(nissa.getCard(), first.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
    }

    @Test
    void triggerStillExilesPermanentsAfterAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new BaneOfBalaGed());
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaVoiceOfZendikar());
        nissa.setCounterCount(CounterType.LOYALTY, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackAt(nissa);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, nissa));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void attackingOwnBattleExilesTheProtectorsPermanents() {
        Permanent attacker = addCreatureReady(player1, new BaneOfBalaGed());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackAt(battle);
            resolveAllTriggers();
        });

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker, battle);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exilesAnEnchantedCreatureAndItsAuraTogether() {
        addCreatureReady(player1, new BaneOfBalaGed());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BaneOfBalaGed());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RealmbreakersGrasp());
        aura.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId(), aura.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(creature.getCard(), aura.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(forest);
    }

    private void declareAttackAt(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, target.getId()));
    }
}
