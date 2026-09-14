package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelOfTheDawn;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonlordDromoka;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonlordDromoka.class, KaaliaOfTheVast.class, AngelOfTheDawn.class, GrizzlyBears.class, ChandraNalaar.class})
class KaaliaOfTheVastTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking an opponent puts an Angel from hand onto the battlefield tapped and attacking")
    void putsEligibleCreatureTappedAndAttacking() {
        Permanent kaalia = addCreatureReady(player1, new KaaliaOfTheVast());
        harness.setHand(player1, List.of(new AngelOfTheDawn()));

        declareAttackers(player1, List.of(0), Map.of(0, player2.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent angel = findPermanent(player1, "Angel of the Dawn");
        assertThat(angel).isNotNull();
        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.isAttackedThisTurn()).isTrue();
        assertThat(angel.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(kaalia.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Only Angel, Demon, or Dragon creature cards are offered")
    void offersOnlyEligibleCreatureCards() {
        addCreatureReady(player1, new KaaliaOfTheVast());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AngelOfTheDawn()));

        declareAttackers(player1, List.of(0), Map.of(0, player2.getId()));
        resolveAllTriggers();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Attacking a planeswalker does not trigger Kaalia")
    void doesNotTriggerWhenAttackingPlaneswalker() {
        addCreatureReady(player1, new KaaliaOfTheVast());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.setHand(player1, List.of(new AngelOfTheDawn()));

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof AngelOfTheDawn);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
    @Test
    @DisplayName("Attacking puts an Angel, Demon, or Dragon from hand onto the battlefield tapped and attacking")
    void putsDragonTappedAndAttacking() {
        harness.setHand(player1, List.of(new DragonlordDromoka()));
        attackWithKaalia();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent dragon = findPermanent(player1, "Dragonlord Dromoka");
        assertThat(dragon).isNotNull();
        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only Angel, Demon, or Dragon creature cards are offered")
    void offersOnlyMatchingCreatures() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new DragonlordDromoka()));
        attackWithKaalia();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the matching card in hand")
    void decliningLeavesCardInHand() {
        harness.setHand(player1, List.of(new DragonlordDromoka()));
        attackWithKaalia();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Dragonlord Dromoka");
    }

    private void attackWithKaalia() {
        Permanent kaalia = new Permanent(new KaaliaOfTheVast());
        kaalia.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaalia);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        harness.passBothPriorities();
    }
}
