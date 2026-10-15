package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DoomsdayExcruciator;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnholyAnnexRitualChamber.class, DoomsdayExcruciator.class, Swamp.class})
class UnholyAnnexRitualChamberTest extends BaseCardTest {

    @Test
    void unholyAnnexDrawsAndYouLoseLifeWithoutADemon() {
        Card draw = new Card();
        draw.setName("Draw");
        harness.setLibrary(player1, List.of(draw));
        castRoom(0);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());
        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    void unholyAnnexDrawsDrainsOpponentsAndGainsLifeWithADemon() {
        Card demon = new Card();
        demon.setName("Demon");
        demon.setType(CardType.CREATURE);
        demon.setColor(CardColor.BLACK);
        demon.setSubtypes(List.of(CardSubtype.DEMON));
        demon.setPower(2);
        demon.setToughness(2);
        harness.addToBattlefield(player1, demon);

        Card draw = new Card();
        draw.setName("Draw");
        harness.setLibrary(player1, List.of(draw));
        castRoom(0);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());
        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
    }

    @Test
    void ritualChamberCreatesADemonTokenWhenUnlocked() {
        castRoom(1);

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getEffectivePower()).isEqualTo(6);
        assertThat(demon.getEffectiveToughness()).isEqualTo(6);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(demon.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void lockedAnnexDoesNotTriggerWhenOnlyRitualChamberWasCast() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        castRoom(1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void roomEnteringWithoutBeingCastHasNoEndStepAbility() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        harness.addToBattlefield(player1, new UnholyAnnexRitualChamber());
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    void unlockingRitualChamberAfterCastingAnnexCreatesExactlyOneDemon() {
        Permanent room = castRoom(0);
        assertThat(countPermanents(player1, "Demon")).isZero();
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getEffectivePower()).isEqualTo(6);
        assertThat(demon.getEffectiveToughness()).isEqualTo(6);
        assertThat(demon.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void unlockingAnnexAfterCastingChamberEnablesTheEndStepAbilityWithoutAnotherToken() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        Permanent room = castRoom(1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        resolveAllTriggers();
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    void annexDoesNotTriggerDuringOpponentsEndStep() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        castRoom(0);
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void opponentsDemonDoesNotEnableLifeDrain() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        harness.addToBattlefield(player2, new DoomsdayExcruciator());
        castRoom(0);
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void demonIsCheckedWhenEndStepAbilityResolves() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        castRoom(0);
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());
        forceEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new DoomsdayExcruciator());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
    }

    @Test
    void losingTheDemonBeforeResolutionCausesLifeLossInsteadOfDrain() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DoomsdayExcruciator());
        castRoom(0);
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());
        forceEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(demon);
        gd.playerGraveyards.get(player1.getId()).add(demon.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void removingRoomDoesNotStopAnAlreadyTriggeredEndStepAbility() {
        Card draw = new Swamp();
        harness.setLibrary(player1, List.of(draw));
        Permanent room = castRoom(0);
        int ownLife = gd.getLife(player1.getId());
        forceEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(room);
        gd.playerGraveyards.get(player1.getId()).add(room.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife - 2);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new UnholyAnnexRitualChamber()));
        harness.addMana(player1, ManaColor.BLACK, doorIndex == 0 ? 3 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Unholy Annex // Ritual Chamber");
    }

    private void forceEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
