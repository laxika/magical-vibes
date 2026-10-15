package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OsseousSticktwister;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaltzOfRage.class, HillGiant.class, GrizzlyBears.class, Shock.class,
        OsseousSticktwister.class, GiantGrowth.class, Mountain.class})
class WaltzOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to every other creature and exiles one card for each controlled creature that dies")
    void damagesOtherCreaturesAndExilesForControlledDeaths() {
        Card firstCard = new Shock();
        Card secondCard = new Shock();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(sourceId));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard);
        assertThat(gd.exilePlayPermissions).containsEntry(firstCard.getId(), player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, firstCard.getId(), player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID opponentCreatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentCreatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void exilesOneCardForEachOfMultipleSimultaneousControlledDeaths() {
        Card firstCard = new Shock();
        Card secondCard = new Shock();
        Card thirdCard = new Shock();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(sourceId));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.exilePlayPermissions).containsEntry(firstCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void laterDeathOfTheTargetCreatureAlsoTriggersExile() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, List.of(sourceId));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, sourceId);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void illegalTargetPreventsDamageAndRegistrationOfDeathTrigger() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID survivorId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, List.of(sourceId));
        harness.castAndResolveInstant(player2, 0, sourceId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Waltz of Rage");
        harness.castInstant(player2, 0, survivorId);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void damageUsesTheCreatureAsSourceAndAppliesItsLifelink() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new OsseousSticktwister()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(sourceId));
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Osseous Sticktwister");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void emptyLibraryDoesNotPreventCreatureDamage() {
        harness.setLibrary(player1, List.of());
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(sourceId));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void usesPowerAtResolutionRatherThanWhenCast() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new WaltzOfRage(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(sourceId));
        harness.castAndResolveInstant(player1, 0, sourceId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void exiledLandCanBePlayedButNormalLandLimitStillApplies() {
        Card firstLand = new Mountain();
        Card secondLand = new Mountain();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, List.of(sourceId));
        resolveAllTriggers();

        harness.castFromExile(player1, firstLand.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(secondLand);
        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
    }

    @Test
    void deathTriggerExpiresThisTurnButPlayPermissionLastsThroughNextTurn() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new HillGiant()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaltzOfRage()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, List.of(sourceId));
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        UUID laterCreatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, laterCreatureId);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
