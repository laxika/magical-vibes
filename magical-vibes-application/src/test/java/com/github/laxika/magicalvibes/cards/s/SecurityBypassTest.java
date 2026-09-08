package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecurityBypass.class, GrizzlyBears.class, Mountain.class})
class SecurityBypassTest extends BaseCardTest {

    @Test
    void enchantedCreatureCannotBeBlockedWhenAttackingAlone() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(attacker);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, attacker)));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void enchantedCreatureCanBeBlockedWhenAnotherCreatureAttacks() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(attacker);
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, attacker), indexOf(player1, otherAttacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enchantedCreatureConnivesAndGetsCounterForNonlandDiscard() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(attacker);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
    }

    @Test
    void enchantedCreatureConnivesWithoutCounterForLandDiscard() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(attacker);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mountain");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new SecurityBypass());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
