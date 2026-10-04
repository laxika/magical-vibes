package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AvatarAang;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireLordOzai.class, GrizzlyBears.class, AvatarAang.class, Mountain.class})
class FireLordOzaiTest extends BaseCardTest {

    @Test
    void attackingMaySacrificeAnotherCreatureForRedManaEqualToItsPower() {
        Permanent ozai = addReadyOzai();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ozai);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void activatedAbilityExilesEachOpponentTopCardAndGrantsOneFreePlay() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        Permanent ozai = harness.addToBattlefieldAndReturn(player1, new FireLordOzai());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ozai), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.hasExilePlayPermissionRemaining(topCard.getId())).isTrue();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void attackingWithoutAcceptingDoesNotSacrifice() {
        addReadyOzai();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addReadyOzai() {
        return addCreatureReady(player1, new FireLordOzai());
    }

    @Test
    void sacrificeAddsManaDuringTheOriginalAttackTriggerResolution() {
        addReadyOzai();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, bears.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeManaDoesNotTriggerAvatarAangsBendingAbility() {
        addReadyOzai();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AvatarAang());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void sacrificeUsesPowerIncludingCountersAndManaSurvivesCombatSteps() {
        addReadyOzai();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void attackCannotSacrificeOzaiOrAnOpponentsCreature() {
        Permanent ozai = addReadyOzai();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ozai);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    void exiledCreatureStillRequiresNormalSorceryTiming() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new FireLordOzai());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(topCard);
    }

    @Test
    void unusedFreePlayPermissionExpiresAtEndOfTurnButCardStaysExiled() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));
        harness.addToBattlefield(player1, new FireLordOzai());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    void activatedAbilityCanPlayAnExiledLandUsingTheNormalLandPlay() {
        Mountain topCard = new Mountain();
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new FireLordOzai());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(topCard);
    }

    @Test
    void activatedAbilityDoesNothingToAnEmptyOpponentLibrary() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new FireLordOzai());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
