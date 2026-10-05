package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WringFlesh;
import com.github.laxika.magicalvibes.cards.w.WithstandDeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Phthisis.class, GiantSpider.class, GrizzlyBears.class, WringFlesh.class, WithstandDeath.class,
        PlatinumEmperion.class})
class PhthisisTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and its controller loses life equal to power plus toughness")
    void destroysCreatureAndControllerLosesPowerPlusToughness() {
        harness.addToBattlefield(player2, new GiantSpider());
        castPhthisis();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Includes negative power when calculating the life loss")
    void includesNegativePowerInLifeLoss() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Causes no life loss when power plus toughness is negative")
    void causesNoLifeLossWhenPowerPlusToughnessIsNegative() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GiantSpider()).getId();

        harness.setHand(player1, List.of(new WringFlesh(), new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Its controller loses life even when the target is indestructible")
    void controllerLosesLifeWhenTargetIsIndestructible() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GiantSpider()).getId();

        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Fizzles without life loss if the target is removed before resolution")
    void fizzlesIfTargetIsRemoved() {
        harness.addToBattlefield(player2, new GiantSpider());
        castPhthisis();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Suspend exiles Phthisis with five time counters and later offers a free cast")
    void suspendOffersFreeCast() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GiantSpider()).getId();
        Phthisis card = suspendPhthisis();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        advanceThroughSuspendCounters();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Phthisis");
    }

    @Test
    @DisplayName("Suspend counters are removed only during Phthisis's owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        Phthisis card = suspendPhthisis();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Phthisis in exile without time counters")
    void decliningSuspendCastLeavesCardInExile() {
        Phthisis card = suspendPhthisis();

        advanceThroughSuspendCounters();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    @DisplayName("Destroys Platinum Emperion before applying life loss")
    void destroysLifeTotalRestrictionBeforeLifeLoss() {
        harness.addToBattlefield(player2, new PlatinumEmperion());
        castPhthisis();
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Platinum Emperion");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Platinum Emperion");
        harness.assertLife(player2, 4);
    }

    @Test
    @DisplayName("Negative power can still produce positive life loss")
    void addsNegativePowerToPositiveToughness() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GiantSpider()).getId();
        harness.setHand(player1, List.of(new WringFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        castPhthisis();
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Targeting your own creature makes you lose life")
    void ownCreatureControllerLosesLife() {
        harness.addToBattlefield(player1, new GiantSpider());
        castPhthisis();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player1, "Giant Spider");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    private Phthisis suspendPhthisis() {
        Phthisis card = new Phthisis();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void advanceThroughSuspendCounters() {
        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
    }

    private void castPhthisis() {
        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 7);
    }
}
