package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtinguisherBattleship.class, Forest.class, GrizzlyBears.class})
class ExtinguisherBattleshipTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it destroys a noncreature permanent and deals 4 damage to each creature")
    void entersDestroysPermanentAndDamagesCreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBattleship(forest.getId());

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Extinguisher Battleship");
    }

    @Test
    @DisplayName("Cannot target a creature with its enter-the-battlefield ability")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExtinguisherBattleship()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1,
                new ExtinguisherBattleship());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(battleship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(battleship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Five charge counters make it an artifact creature with flying and trample")
    void fiveChargeCountersUnlockAbilities() {
        Permanent battleship = harness.addToBattlefieldAndReturn(player1,
                new ExtinguisherBattleship());

        battleship.setCounterCount(CounterType.CHARGE, 4);
        assertThat(gqs.isCreature(gd, battleship)).isFalse();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.TRAMPLE)).isFalse();

        battleship.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, battleship)).isTrue();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, battleship, Keyword.TRAMPLE)).isTrue();
    }

    private void castBattleship(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ExtinguisherBattleship()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
