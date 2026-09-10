package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullSkaab.class, GrizzlyBears.class})
class SkullSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not create a Zombie")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSkullSkaab();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Skull Skaab");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(zombieTokens()).isZero();
    }

    @Test
    @DisplayName("Exploiting a nontoken creature creates a 2/2 black Zombie token")
    void exploitingNontokenCreatureCreatesZombie() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSkullSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(findPermanents(player1, "Zombie").getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Exploiting a token does not create a Zombie")
    void exploitingTokenDoesNotCreateZombie() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, tokenCard);

        castSkullSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        assertThat(zombieTokens()).isZero();
        harness.assertOnBattlefield(player1, "Skull Skaab");
    }

    @Test
    @DisplayName("Sacrificing Skull Skaab itself still creates a Zombie for a nontoken exploit")
    void sacrificingItselfStillCreatesZombie() {
        castSkullSkaab();
        Permanent skullSkaab = findPermanent(player1, "Skull Skaab");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, skullSkaab.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skull Skaab");
        assertThat(zombieTokens()).isEqualTo(1);
    }

    private long zombieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Zombie".equals(permanent.getCard().getName()))
                .count();
    }

    private void castSkullSkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SkullSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
