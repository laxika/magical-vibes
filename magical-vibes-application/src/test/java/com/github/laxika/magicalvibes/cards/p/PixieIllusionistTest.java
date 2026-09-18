package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PixieIllusionist.class, Forest.class, GrizzlyBears.class})
class PixieIllusionistTest extends BaseCardTest {

    @Test
    void castWithoutKickerEntersWithoutCounters() {
        harness.setHand(player1, List.of(new PixieIllusionist()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPixie().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithTwoCounters() {
        harness.setHand(player1, List.of(new PixieIllusionist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPixie().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tappedAbilityMakesControlledLandTheChosenBasicTypeUntilEndOfTurn() {
        addCreatureReady(player1, new PixieIllusionist());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    void abilityCannotTargetOpponentLand() {
        addCreatureReady(player1, new PixieIllusionist());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    void abilityCannotTargetNonLandPermanent() {
        addCreatureReady(player1, new PixieIllusionist());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent findPixie() {
        return findPermanent(player1, "Pixie Illusionist");
    }
}
