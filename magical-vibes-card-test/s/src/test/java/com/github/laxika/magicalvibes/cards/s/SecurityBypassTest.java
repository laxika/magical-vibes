package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecurityBypass.class, ChromeCat.class, Mountain.class})
class SecurityBypassTest extends BaseCardTest {

    @Test
    void enchantedCreatureCannotBeBlockedWhenAttackingAlone() {
        Permanent attacker = addCreatureReady(player1, new ChromeCat());
        attachAura(attacker);
        Permanent blocker = addCreatureReady(player2, new ChromeCat());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void enchantedCreatureCanBeBlockedWhenAnotherCreatureAttacks() {
        Permanent attacker = addCreatureReady(player1, new ChromeCat());
        attachAura(attacker);
        Permanent otherAttacker = addCreatureReady(player1, new ChromeCat());
        Permanent blocker = addCreatureReady(player2, new ChromeCat());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker), indexOf(player1, otherAttacker)));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enchantedCreatureConnivesAndGetsCounterForNonlandDiscard() {
        Permanent attacker = addCreatureReady(player1, new ChromeCat());
        attachAura(attacker);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new ChromeCat()));

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Chrome Cat");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
    }

    @Test
    void enchantedCreatureConnivesWithoutCounterForLandDiscard() {
        Permanent attacker = addCreatureReady(player1, new ChromeCat());
        attachAura(attacker);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new ChromeCat()));

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mountain");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Chrome Cat");
    }

    @Test
    void auraCanEnchantAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new ChromeCat());
        harness.setHand(player1, List.of(new SecurityBypass()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Security Bypass").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void creatureControllerConnivesRatherThanAuraController() {
        Permanent attacker = addCreatureReady(player2, new ChromeCat());
        attachAura(attacker);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new ChromeCat()));

        declareAttackers(player2, List.of(indexOf(player2, attacker)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Chrome Cat");
        harness.handleCardChosen(player2, 1);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void creatureStillConnivesWhenAttackingWithAnotherCreature() {
        Permanent attacker = addCreatureReady(player1, new ChromeCat());
        attachAura(attacker);
        Permanent otherAttacker = addCreatureReady(player1, new ChromeCat());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new ChromeCat()));

        declareAttackers(List.of(indexOf(player1, attacker), indexOf(player1, otherAttacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Chrome Cat");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SecurityBypass());
        aura.setAttachedTo(creature.getId());
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
