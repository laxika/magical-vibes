package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndeadAugur.class, DiregrafGhoul.class, GrizzlyBears.class, Shock.class})
class UndeadAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Another Zombie you control dying draws a card and costs 1 life")
    void anotherZombieDeathTriggers() {
        harness.addToBattlefield(player1, new UndeadAugur());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        Card drawn = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player2, player1, "Diregraf Ghoul");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A non-Zombie creature dying does not trigger")
    void nonZombieDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new UndeadAugur());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card topCard = new DiregrafGhoul();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Undead Augur dying draws a card and costs 1 life")
    void ownDeathTriggers() {
        harness.addToBattlefield(player1, new UndeadAugur());
        Card drawn = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player2, player1, "Undead Augur");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
