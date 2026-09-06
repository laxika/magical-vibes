package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CavePeople;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SibilantSpirit.class, CavePeople.class, Forest.class, GrizzlyBears.class})
class SibilantSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks a player, that defending player may draw (accept)")
    void defendingPlayerDraws() {
        addCreatureReady(player1, new SibilantSpirit());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve attack trigger → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int attackingPlayerHandBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(attackingPlayerHandBefore);
    }

    @Test
    @DisplayName("Defending player may decline the draw")
    void defendingPlayerDeclines() {
        addCreatureReady(player1, new SibilantSpirit());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("Attacking a planeswalker still offers the draw to its controller")
    void attackingPlaneswalkerOffersDrawToController() {
        addCreatureReady(player1, new SibilantSpirit());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackersAtTarget(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for another attacking creature")
    void doesNotTriggerForOtherAttacker() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its attack trigger is controlled by the attacking player for trigger ordering")
    void attackTriggerUsesAttackingPlayerForApnapOrdering() {
        addCreatureReady(player1, new SibilantSpirit());
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());
        harness.setLibrary(player2, List.of(new Forest()));
        int cavePowerBefore = gqs.getEffectivePower(gd, cavePeople);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cavePeople)).isEqualTo(cavePowerBefore + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Uses the current controller of an attacked planeswalker when the trigger resolves")
    void attackedPlaneswalkerControllerIsResolvedAtResolution() {
        addCreatureReady(player1, new SibilantSpirit());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackersAtTarget(player1, List.of(0), Map.of(0, planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(player2HandBefore);
    }

    @Test
    @DisplayName("Uses a battle's protector as the defending player")
    void attackingBattleOffersDrawToProtector() {
        addCreatureReady(player1, new SibilantSpirit());

        Card battleCard = new Card();
        battleCard.setName("Test Siege");
        battleCard.setType(CardType.BATTLE);
        battleCard.setDefense(4);
        Permanent battle = new Permanent(battleCard);
        battle.setCounterCount(CounterType.DEFENSE, 4);
        battle.setProtectorPlayerId(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(battle);

        harness.setLibrary(player2, List.of(new Forest()));
        declareAttackersAtTarget(player1, List.of(0), Map.of(0, battle.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    private void declareAttackersAtTarget(Player player, List<Integer> attackerIndices,
                                          Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

}
