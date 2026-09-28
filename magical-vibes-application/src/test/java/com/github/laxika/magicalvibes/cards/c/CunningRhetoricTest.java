package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({CunningRhetoric.class, Divination.class, GrizzlyBears.class})
class CunningRhetoricTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one card when multiple creatures attack and grants indefinite play permission")
    void exilesOneCardForAnAttack() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0, 1), null);
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isFalse();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent attacks a planeswalker you control")
    void triggersForPlaneswalkerAttack() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        Permanent planeswalker = addTestPlaneswalker(player1);
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The controller can cast the exiled spell using mana of any color")
    void castsExiledSpellWithAnyColorMana() {
        harness.addToBattlefield(player1, new CunningRhetoric());
        addReadyCreature(player2);
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new Divination(), new Divination()));
        harness.setHand(player1, List.of());

        declareAttackers(player2, List.of(0), null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void addReadyCreature(Player player) {
        addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addTestPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(4);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
