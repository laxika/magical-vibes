package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NahiriTheUnforgivingTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes the target attack a player rather than a planeswalker each combat")
    void plusOneRequiresAttackingAPlayer() {
        Permanent nahiri = addReadyNahiri(3);
        Permanent planeswalker = addPlaneswalker(player1);
        Permanent creature = addCreature(player2, "Bear");

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(creatureIndex),
                Map.of(creatureIndex, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player2, List.of(creatureIndex),
                Map.of(creatureIndex, player1.getId()));
    }

    @Test
    @DisplayName("+1 rummages")
    void plusOneDiscardsThenDraws() {
        addReadyNahiri(3);
        Card discarded = new Shock();
        Card kept = new GrizzlyBears();
        Card drawn = new Shock();
        harness.setHand(player1, List.of(discarded, kept));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("0 creates a hasty temporary copy from a qualifying graveyard card")
    void zeroCreatesHastyCopyAndExilesItAtNextEndStep() {
        addReadyNahiri(3);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("0 rejects a graveyard card whose mana value is not below loyalty")
    void zeroRejectsCardAtOrAboveLoyalty() {
        addReadyNahiri(3);
        Card expensive = new Card() {
        };
        expensive.setName("Expensive Creature");
        expensive.setType(CardType.CREATURE);
        expensive.setManaCost("{3}");
        expensive.setPower(3);
        expensive.setToughness(3);
        harness.setGraveyard(player1, List.of(expensive));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(expensive.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyNahiri(int loyalty) {
        Permanent nahiri = harness.addToBattlefieldAndReturn(player1, new NahiriTheUnforgiving());
        nahiri.setCounterCount(CounterType.LOYALTY, loyalty);
        nahiri.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nahiri;
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Card card = new Card() {
        };
        card.setName("Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, String name) {
        Card card = new Card() {
        };
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
