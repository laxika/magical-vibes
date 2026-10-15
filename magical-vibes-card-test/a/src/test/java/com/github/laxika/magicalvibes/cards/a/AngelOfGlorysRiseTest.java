package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.c.ChampionOfLambholt;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HauntedGuardian;
import com.github.laxika.magicalvibes.cards.h.HuntedGhoul;
import com.github.laxika.magicalvibes.cards.m.MidnightDuelist;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfGlorysRise.class, DiregrafGhoul.class, EliteVanguard.class,
        GrizzlyBears.class, SavannahLions.class, WalkingCorpse.class,
        HauntedGuardian.class, HuntedGhoul.class, MidnightDuelist.class, ChampionOfLambholt.class})
class AngelOfGlorysRiseTest extends BaseCardTest {

    private void castAngel() {
        harness.castFromHand(player1, new AngelOfGlorysRise(), "{5}{W}{W}");
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB exiles all Zombies on the battlefield, regardless of controller")
    void etbExilesAllZombies() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAngel();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Diregraf Ghoul");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns all Human creature cards from your graveyard to the battlefield")
    void etbReturnsHumansFromOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new EliteVanguard(), new SavannahLions()));

        castAngel();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
        harness.assertNotInGraveyard(player1, "Elite Vanguard");
        harness.assertInGraveyard(player1, "Savannah Lions");
    }

    @Test
    @DisplayName("Humans in an opponent's graveyard are not returned")
    void opponentHumansStayInGraveyard() {
        harness.setGraveyard(player2, List.of(new EliteVanguard()));

        castAngel();

        harness.assertInGraveyard(player2, "Elite Vanguard");
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Resolves with no Zombies and an empty graveyard")
    void resolvesWithNothingToDo() {
        castAngel();

        harness.assertOnBattlefield(player1, "Angel of Glory's Rise");
    }

    @Test
    @DisplayName("Exiles battlefield Zombies and returns every Human without exiling graveyard Zombies")
    void exilesZombiesAndReturnsMultipleHumans() {
        MidnightDuelist firstHuman = new MidnightDuelist();
        MidnightDuelist secondHuman = new MidnightDuelist();
        HuntedGhoul battlefieldZombie = new HuntedGhoul();
        HuntedGhoul graveyardZombie = new HuntedGhoul();
        harness.addToBattlefield(player2, battlefieldZombie);
        harness.setGraveyard(player1, List.of(firstHuman, secondHuman, graveyardZombie, new HauntedGuardian()));

        castAngel();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstHuman.getId(), secondHuman.getId());
        harness.assertNotInGraveyard(player1, "Midnight Duelist");
        harness.assertInGraveyard(player1, "Hunted Ghoul");
        harness.assertInGraveyard(player1, "Haunted Guardian");
        harness.assertNotOnBattlefield(player2, "Hunted Ghoul");
        assertThat(gd.findExiledCard(battlefieldZombie.getId())).isNotNull();
        assertThat(gd.findExiledCard(graveyardZombie.getId())).isNull();
    }

    @Test
    @DisplayName("Returned Humans see other Humans entering simultaneously")
    void returnedChampionSeesHumanEarlierInGraveyard() {
        harness.setGraveyard(player1, List.of(new MidnightDuelist(), new ChampionOfLambholt()));

        castAngel();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Champion of Lambholt"))
                .findFirst().orElseThrow()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
