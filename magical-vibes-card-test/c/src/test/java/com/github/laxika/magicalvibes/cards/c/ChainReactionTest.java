package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainReaction.class, CentaurCourser.class, HillGiant.class, GrizzlyBears.class, Forest.class})
class ChainReactionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of creatures on the battlefield")
    void dealsDamageEqualToCreatureCountAcrossAllPlayers() {
        harness.addToBattlefield(player1, new CentaurCourser());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChainReaction()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Centaur Courser");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts creatures rather than all permanents and does not damage players")
    void countsOnlyCreaturesAndDoesNotDamagePlayers() {
        harness.addToBattlefield(player1, new CentaurCourser());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChainReaction()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses the creature count at resolution, including creatures that entered after casting")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new CentaurCourser());
        harness.setHand(player1, List.of(new ChainReaction()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        assertThat(findPermanent(player1, "Centaur Courser").getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chain Reaction");
    }

    @Test
    @DisplayName("Deals one damage when there is only one creature")
    void dealsOneDamageToLoneCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChainReaction()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves with no creatures and leaves other permanents and players unharmed")
    void resolvesWithNoCreatures() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ChainReaction()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Chain Reaction");
    }
}
