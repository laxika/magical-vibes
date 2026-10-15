package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CharredFoyerWarpedSpace;
import com.github.laxika.magicalvibes.cards.g.GetOut;
import com.github.laxika.magicalvibes.cards.l.LeylineOfResonance;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmokyLoungeMistySalon.class, LeylineOfResonance.class, CharredFoyerWarpedSpace.class, GetOut.class})
class SmokyLoungeMistySalonTest extends BaseCardTest {

    @Test
    void smokyLoungeAddsTwoRedRoomOnlyManaAtTheControllersFirstMainPhase() {
        castSmokyLounge();

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId())
                .getRoomSpellsOrUnlocksMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void roomOnlyManaCanCastARoomButNotAnotherEnchantment() {
        castSmokyLounge();
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new LeylineOfResonance()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new CharredFoyerWarpedSpace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CharredFoyerWarpedSpace);
    }

    @Test
    void mistySalonCreatesAnXSpiritUsingAllUnlockedRoomDoors() {
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon(), new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(spirit.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.FLYING);
    }

    private void castSmokyLounge() {
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void lockedSmokyLoungeDoesNotProduceMana() {
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getRoomSpellsOrUnlocksManaTotal()).isZero();
    }

    @Test
    void smokyLoungeDoesNotProduceManaOnAnOpponentsTurn() {
        castSmokyLounge();
        advanceToPrecombatMain(player2);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getRoomSpellsOrUnlocksManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getRoomSpellsOrUnlocksManaTotal()).isZero();
    }

    @Test
    void restrictedManaCanPayGenericUnlockCostAndBothDoorsCount() {
        castSmokyLounge();
        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.unlockRoomDoor(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getRoomSpellsOrUnlocksManaTotal()).isZero();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void mistySalonIgnoresOpponentsDoorsAndLockedRooms() {
        Permanent opponentRoom = harness.addToBattlefieldAndReturn(player2, new SmokyLoungeMistySalon());
        opponentRoom.unlockRoomDoor(0);
        opponentRoom.unlockRoomDoor(1);
        harness.addToBattlefield(player1, new SmokyLoungeMistySalon());
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void unlockingSmokyLoungeDoesNotCreateAnotherSpiritOrResizeTheExistingOne() {
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.unlockRoomDoor(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getRoomSpellsOrUnlocksManaTotal()).isZero();
    }

    @Test
    void smokyLoungeTriggerStillProducesManaAfterTheRoomIsReturnedToHand() {
        castSmokyLounge();
        advanceToPrecombatMain(player1);
        Permanent room = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 1, List.of(room.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(room);
        assertThat(gd.playerManaPools.get(player1.getId()).getRoomSpellsOrUnlocksMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void mistySalonCountsDoorsAtResolutionAfterAnotherRoomLeaves() {
        castSmokyLounge();
        Permanent smokyLounge = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SmokyLoungeMistySalon(), new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.castModalInstant(player1, 0, 1, List.of(smokyLounge.getId()));
        resolveAllTriggers();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
    }
}
