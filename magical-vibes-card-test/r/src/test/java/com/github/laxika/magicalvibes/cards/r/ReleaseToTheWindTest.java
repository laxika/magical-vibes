package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SilvergillAdept;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReleaseToTheWind.class, RaptorCompanion.class, Island.class, WalkingBallista.class,
        SilvergillAdept.class})
class ReleaseToTheWindTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent and lets its owner cast it for free")
    void exilesPermanentAndGrantsOwnerFreeCast() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        UUID raptorCardId = raptor.getOriginalCard().getId();

        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, raptor.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(raptorCardId));
        assertThat(gd.exilePlayPermissions.get(raptorCardId)).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(raptorCardId);
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(raptorCardId);
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).doesNotContainKey(raptorCardId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, raptorCardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, raptorCardId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Raptor Companion");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(raptorCardId);
    }

    @Test
    @DisplayName("Free casting a creature still requires normal sorcery timing")
    void freeCastRequiresNormalTiming() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        UUID cardId = raptor.getOriginalCard().getId();
        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, raptor.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player2, cardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player2, cardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, cardId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Can exile and recast your own permanent")
    void canExileOwnPermanent() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        UUID cardId = raptor.getOriginalCard().getId();
        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, raptor.getId());
        harness.assertNotOnBattlefield(player1, "Raptor Companion");
        harness.castFromExile(player1, cardId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Raptor Companion");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot choose a nonzero X when casting without paying the mana cost")
    void cannotChooseNonzeroXForFreeCast() {
        Permanent ballista = harness.addToBattlefieldAndReturn(player1, new WalkingBallista());
        ballista.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        UUID cardId = ballista.getOriginalCard().getId();
        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, ballista.getId());

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, cardId, 5, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cardId));

        harness.castFromExile(player1, cardId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Walking Ballista");
        harness.assertInGraveyard(player1, "Walking Ballista");
    }

    @Test
    @DisplayName("Permission survives a turn boundary")
    void permissionSurvivesTurnBoundary() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        UUID cardId = raptor.getOriginalCard().getId();
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, raptor.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player2, cardId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Raptor Companion");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The owner can recast a permanent controlled by another player")
    void permissionBelongsToOwnerRatherThanController() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        UUID cardId = raptor.getOriginalCard().getId();
        gd.stolenCreatures.put(raptor.getId(), player1.getId());
        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, raptor.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cardId));
        assertThatThrownBy(() -> harness.castFromExile(player2, cardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        harness.castFromExile(player1, cardId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertNotOnBattlefield(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Mandatory additional costs still have to be paid")
    void freeCastStillRequiresAdditionalCosts() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SilvergillAdept());
        UUID cardId = adept.getOriginalCard().getId();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, adept.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, cardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cardId));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, cardId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Silvergill Adept");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }
}
