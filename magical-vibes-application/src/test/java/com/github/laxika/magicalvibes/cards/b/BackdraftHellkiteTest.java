package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackdraftHellkite.class, Shock.class, Divination.class, GrizzlyBears.class, FaithlessLooting.class})
class BackdraftHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flashback to instant and sorcery cards in its controller's graveyard")
    void attackingGrantsFlashbackToInstantAndSorceryCards() {
        Permanent hellkite = addCreatureReady(player1, new BackdraftHellkite());
        Shock shock = new Shock();
        Divination divination = new Divination();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, divination, bears));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(hellkite.isAttackedThisTurn()).isTrue();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn)
                .containsExactlyInAnyOrder(shock.getId(), divination.getId())
                .doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Attacking lets its controller cast a graveyard instant with its mana cost as flashback")
    void attackingAllowsCastingGrantedFlashback() {
        addCreatureReady(player1, new BackdraftHellkite());
        Shock shock = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Attacking does not grant flashback to an opponent's graveyard")
    void attackingDoesNotGrantFlashbackToOpponentsGraveyard() {
        addCreatureReady(player1, new BackdraftHellkite());
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(shock.getId());
    }

    @Test
    @DisplayName("Cards with printed flashback also gain the option to pay their mana cost")
    void printedFlashbackDoesNotPreventManaCostFlashback() {
        addCreatureReady(player1, new BackdraftHellkite());
        FaithlessLooting looting = new FaithlessLooting();
        harness.setGraveyard(player1, List.of(looting));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0);

        harness.assertNotInGraveyard(player1, "Faithless Looting");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(looting.getId()));
    }

    @Test
    @DisplayName("Cards entering the graveyard after the trigger resolves do not gain flashback")
    void laterGraveyardCardsDoNotGainFlashback() {
        addCreatureReady(player1, new BackdraftHellkite());
        harness.setGraveyard(player1, List.of());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Granted flashback persists after Backdraft Hellkite leaves the battlefield")
    void grantedFlashbackPersistsWithoutHellkite() {
        Permanent hellkite = addCreatureReady(player1, new BackdraftHellkite());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(hellkite);
        gd.playerGraveyards.get(player1.getId()).add(hellkite.getCard());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }
}
