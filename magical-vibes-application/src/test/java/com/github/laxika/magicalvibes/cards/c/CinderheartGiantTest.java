package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinderheartGiantTest extends BaseCardTest {

    private void killGiant(Permanent giant) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When it dies, it deals 7 damage to an opponent's randomly chosen creature")
    void deathTriggerDamagesRandomOpponentCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killGiant(giant);

        harness.assertInGraveyard(player1, "Cinderheart Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The random pool contains only creatures controlled by opponents")
    void randomPoolExcludesControllerCreaturesAndNoncreatures() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        killGiant(giant);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendlyCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FountainOfYouth);
    }

    @Test
    @DisplayName("The death trigger does nothing when no opponent controls a creature")
    void noOpponentCreatureMeansNoDamage() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CinderheartGiant());
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killGiant(giant);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendlyCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
