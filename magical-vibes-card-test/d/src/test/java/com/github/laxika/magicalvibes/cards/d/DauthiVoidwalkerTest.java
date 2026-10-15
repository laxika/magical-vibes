package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiVoidwalker.class, Shock.class, GrizzlyBears.class, DressDown.class, DreyKeeper.class,
        WalkingBallista.class})
class DauthiVoidwalkerTest extends BaseCardTest {

    @Test
    void exilesOpponentCardsFromAnywhereWithVoidCounters() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());

        Card exiled = opponentCreature.getCard();
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.exiledCardsWithVoidCounters).containsExactly(exiled.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(exiled);
    }

    @Test
    void choosesOnlyOpponentOwnedVoidCounterCardAndMayPlayItForFreeThisTurn() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        Card opponentCard = new GrizzlyBears();
        Card ownCard = new Shock();
        gd.addToExileWithVoidCounter(player2.getId(), opponentCard);
        gd.addToExileWithVoidCounter(player1.getId(), ownCard);

        advanceToUpkeep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentCard.getId());
        assertThat(choice.withoutPayingManaCost()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(opponentCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(opponentCard.getId());

        harness.castFromExile(player1, opponentCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentCard);
        assertThat(gd.findExiledCard(opponentCard.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(opponentCard.getId());
    }

    @Test
    void removedReplacementAbilityDoesNotExileOpponentSpell() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        harness.addToBattlefield(player1, new DressDown());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.findExiledCard(shock.getId())).isNull();
        assertThat(gd.exiledCardsWithVoidCounters).doesNotContain(shock.getId());
    }

    @Test
    void opponentTokensStillDie() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        harness.enterBattlefieldAndReturn(player2, new DreyKeeper());
        harness.passBothPriorities();
        Permanent squirrel = findPermanent(player2, "Squirrel");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, squirrel.getId());

        assertThat(gd.creatureDeathCountThisTurn.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.exiledCardsWithVoidCounters).doesNotContain(squirrel.getCard().getId());
    }

    @Test
    void ownSpellGoesToGraveyardWithoutVoidCounter() {
        harness.addToBattlefield(player1, new DauthiVoidwalker());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.findExiledCard(shock.getId())).isNull();
        assertThat(gd.exiledCardsWithVoidCounters).doesNotContain(shock.getId());
    }

    @Test
    void chosenCreatureCannotBeCastDuringUpkeep() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Card exiled = new GrizzlyBears();
        gd.addToExileWithVoidCounter(player2.getId(), exiled);
        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMultipleCardsChosen(player1, List.of(exiled.getId())));

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void unusedPermissionExpiresAtEndOfTurnButVoidCounterRemains() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Card exiled = new GrizzlyBears();
        gd.addToExileWithVoidCounter(player2.getId(), exiled);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(exiled.getId());
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.exiledCardsWithVoidCounters).contains(exiled.getId());
    }

    @Test
    void freeCastCannotChooseNonzeroManaCostX() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Card exiled = new WalkingBallista();
        gd.addToExileWithVoidCounter(player2.getId(), exiled);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiled.getId(), 3, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void cannotBeBlockedByCreatureWithoutShadow() {
        addCreatureReady(player1, new DauthiVoidwalker());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    void cannotBlockCreatureWithoutShadow() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new DauthiVoidwalker());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    void canBlockAnotherCreatureWithShadow() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Permanent blocker = addCreatureReady(player2, new DauthiVoidwalker());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
