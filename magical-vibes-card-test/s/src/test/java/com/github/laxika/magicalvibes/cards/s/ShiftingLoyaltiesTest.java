package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuardianBeast;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShiftingLoyalties.class, GrizzlyBears.class, HillGiant.class, Juggernaut.class, Millstone.class})
class ShiftingLoyaltiesTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ShiftingLoyalties()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Exchanges control of two target creatures")
    void exchangesTwoCreatures() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can exchange an artifact creature with a creature")
    void exchangesPermanentsSharingAType() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Juggernaut");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects targets that do not share a card type")
    void rejectsTargetsWithoutSharedCardType() {
        prepare();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a card type");
    }

    @Test
    @DisplayName("Does nothing when both permanents have the same controller")
    void doesNothingWhenBothHaveSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({Swamp.class})
    @DisplayName("Exchanges lands when the opponent's permanent is the first target")
    void exchangesLandsWithOpponentFirst() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Swamp());

        harness.castAndResolveSorcery(player1, 0, List.of(opponents.getId(), own.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponents).doesNotContain(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own).doesNotContain(opponents);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("No part of the exchange happens when either target leaves the battlefield")
    void doesNothingWhenEitherTargetLeaves(boolean firstTargetLeaves) {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        Permanent removed = firstTargetLeaves ? own : opponents;
        gd.playerBattlefields.get(firstTargetLeaves ? player1.getId() : player2.getId()).remove(removed);
        harness.passBothPriorities();

        if (firstTargetLeaves) {
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        } else {
            harness.assertOnBattlefield(player1, "Hill Giant");
            harness.assertNotOnBattlefield(player2, "Hill Giant");
        }
        harness.assertInGraveyard(player1, "Shifting Loyalties");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @CardUsed({GuardianBeast.class})
    @DisplayName("No part of an exchange happens when Guardian Beast prevents one control change")
    void doesNothingWhenOneControlChangeIsPrevented(boolean protectedTargetFirst) {
        prepare();
        harness.addToBattlefield(player1, new GuardianBeast());
        Permanent protectedArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        List<java.util.UUID> targets = protectedTargetFirst
                ? List.of(protectedArtifact.getId(), opponents.getId())
                : List.of(opponents.getId(), protectedArtifact.getId());

        harness.castAndResolveSorcery(player1, 0, targets);

        harness.assertOnBattlefield(player1, "Millstone");
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertOnBattlefield(player2, "Juggernaut");
        harness.assertNotOnBattlefield(player1, "Juggernaut");
    }
}
