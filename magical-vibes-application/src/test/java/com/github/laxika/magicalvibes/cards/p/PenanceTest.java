package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PenanceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a card from hand on top of the library and prevents the next damage from a chosen black source")
    void preventsDamageFromChosenBlackSource() {
        harness.setLife(player1, 20);
        addPenance(player1);
        Card chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(chosenCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent zombie = addReady(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        zombie.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosenCard);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen red source")
    void preventsDamageFromChosenRedSource() {
        harness.setLife(player1, 20);
        addPenance(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent giant = addReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        giant.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A source that is neither black nor red cannot be chosen")
    void nonBlackOrRedSourceCannotBeChosen() {
        addPenance(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    private Permanent addPenance(Player player) {
        Permanent permanent = new Permanent(new Penance());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
