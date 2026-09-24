package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.s.SurrakElusiveHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidShrine.class, AvenFisher.class, AvenFlock.class, SurrakElusiveHunter.class})
class CephalidShrineTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its caster cannot pay for matching graveyard cards")
    void countersWhenCasterCannotPay() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new AvenFisher()));
        harness.setGraveyard(player2, List.of(new AvenFisher(), new AvenFlock()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenFisher(), "{3}{U}");
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Aven Fisher"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Aven Fisher");
    }

    @Test
    @DisplayName("The caster may pay the number of matching cards in all graveyards")
    void casterMayPayDynamicCost() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new AvenFisher()));
        harness.setGraveyard(player2, List.of(new AvenFisher(), new AvenFlock()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenFisher(), "{3}{U}");
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Aven Fisher");
    }

    @Test
    @DisplayName("Triggers when the shrine's controller casts a spell")
    void triggersOnControllerSpell() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new AvenFisher(), new AvenFisher()));
        harness.forceActivePlayer(player1);

        harness.castFromHand(player1, new AvenFisher(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertNotOnBattlefield(player1, "Aven Fisher");
    }

    @Test
    @DisplayName("Declining to pay counters a spell when the caster can afford the cost")
    void decliningToPayCountersSpell() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new AvenFisher()));
        harness.forceActivePlayer(player1);

        harness.castFromHand(player1, new AvenFisher(), "{3}{U}");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertNotOnBattlefield(player1, "Aven Fisher");
    }

    @Test
    @DisplayName("Counts matching graveyard cards when the trigger resolves")
    void evaluatesGraveyardCountAtResolution() {
        harness.addToBattlefield(player1, new CephalidShrine());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenFisher(), "{3}{U}");
        harness.setGraveyard(player1, List.of(new AvenFisher(), new AvenFisher()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Fisher");
        harness.assertNotOnBattlefield(player2, "Aven Fisher");
    }

    @Test
    @DisplayName("Does not make the triggering spell a target")
    void doesNotTriggerTargetedSpellTriggers() {
        harness.addToBattlefield(player1, new SurrakElusiveHunter());
        harness.addToBattlefield(player2, new CephalidShrine());
        harness.setGraveyard(player1, List.of(new AvenFisher()));
        harness.setLibrary(player1, List.of(new AvenFlock()));
        harness.forceActivePlayer(player1);

        harness.castFromHand(player1, new AvenFisher(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Aven Flock"));
        harness.assertInGraveyard(player1, "Aven Fisher");
    }
}
