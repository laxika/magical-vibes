package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringFriendship.class, GrizzlyBears.class, Divination.class,
        Forest.class, DoomBlade.class, Disenchant.class, OneWithTheStars.class})
class EnduringFriendshipTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts Otter and enchantment creatures when you cast an instant or sorcery")
    void boostsOttersAndEnchantmentsOnInstantOrSorceryCast() {
        Permanent friendship = addCreatureReady(player1, new EnduringFriendship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, friendship)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Double team conjures a copy without double team and removes it from the attacker")
    void doubleTeamConjuresCopyAndIsRemoved() {
        Permanent friendship = addCreatureReady(player1, new EnduringFriendship());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Enduring Friendship")
                        && !card.hasKeyword(Keyword.DOUBLE));
        assertThat(gqs.hasKeyword(gd, friendship, Keyword.DOUBLE)).isFalse();
    }

    @Test
    @DisplayName("Returns from the graveyard as an enchantment")
    void returnsAsEnchantmentOnly() {
        harness.addToBattlefield(player1, new EnduringFriendship());
        Permanent friendship = findPermanent(player1, "Enduring Friendship");

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, friendship.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Enduring Friendship");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("Does not return when it dies as a noncreature")
    void doesNotReturnWhenItWasNotACreature() {
        harness.addToBattlefield(player1, new EnduringFriendship());
        Permanent friendship = findPermanent(player1, "Enduring Friendship");

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, friendship.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, friendship)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, friendship.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enduring Friendship");
        harness.assertNotOnBattlefield(player1, "Enduring Friendship");
    }
}
