package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderousWrath.class, MoorlandInquisitor.class, TibaltTheFiendBlooded.class})
class ThunderousWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target player")
    void deals5DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 5 damage to target creature, destroying it")
    void deals5DamageToCreature() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Moorland Inquisitor");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Moorland Inquisitor");
        harness.assertInGraveyard(player2, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("Miracle cast for {R} off the first draw deals 5 damage")
    void miracleCastDealsDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.setLibrary(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal

        harness.passBothPriorities(); // resolve miracle trigger → cast prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Moorland Inquisitor"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the miracle reveal leaves the card in hand")
    void decliningRevealLeavesInHand() {
        ThunderousWrath wrath = new ThunderousWrath();
        harness.setLibrary(player1, List.of(wrath));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(wrath.getId()));
    }

    @Test
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player2, new TibaltTheFiendBlooded());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Tibalt, the Fiend-Blooded"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tibalt, the Fiend-Blooded");
        harness.assertInGraveyard(player2, "Tibalt, the Fiend-Blooded");
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Thunderous Wrath");
    }

    @Test
    void secondDrawDoesNotOfferMiracle() {
        harness.setLibrary(player1, List.of(new MoorlandInquisitor(), new ThunderousWrath()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Thunderous Wrath");
    }

    @Test
    void firstDrawOnOpponentsTurnCanBeCastForMiracleCost() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.setLibrary(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Moorland Inquisitor"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Thunderous Wrath");
        harness.assertNotInHand(player1, "Thunderous Wrath");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningMiracleCastLeavesCardInHandAndManaUnspent() {
        harness.setLibrary(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Thunderous Wrath");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void miracleCannotBeCastWithoutRedMana() {
        harness.setLibrary(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Thunderous Wrath");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
