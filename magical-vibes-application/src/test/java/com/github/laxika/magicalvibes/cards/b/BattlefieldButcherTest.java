package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PowerstoneFracture;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldButcher.class, PowerstoneFracture.class})
class BattlefieldButcherTest extends BaseCardTest {

    @Test
    void eachCreatureCardInGraveyardReducesActivationCost() {
        addCreatureReady(player1, new BattlefieldButcher());
        harness.setGraveyard(player1, List.of(
                new BattlefieldButcher(), new BattlefieldButcher(), new BattlefieldButcher(), new PowerstoneFracture()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void emptyGraveyardRequiresFiveManaAndTapsAsACost() {
        var butcher = addCreatureReady(player1, new BattlefieldButcher());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(butcher.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    void fiveOrMoreCreatureCardsAllowActivationWithoutMana(int creatureCount) {
        var butcher = addCreatureReady(player1, new BattlefieldButcher());
        harness.setGraveyard(player1, IntStream.range(0, creatureCount)
                .<Card>mapToObj(i -> new BattlefieldButcher()).toList());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(butcher.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsCreaturesAndOwnNoncreatureCardsDoNotReduceCost() {
        var butcher = addCreatureReady(player1, new BattlefieldButcher());
        harness.setGraveyard(player1, List.of(new PowerstoneFracture()));
        harness.setGraveyard(player2, List.of(new BattlefieldButcher(), new BattlefieldButcher()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(butcher.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void tappedCreatureCannotActivateEvenWithFullReduction() {
        var butcher = addCreatureReady(player1, new BattlefieldButcher());
        butcher.tap();
        harness.setGraveyard(player1, List.of(new BattlefieldButcher(), new BattlefieldButcher(),
                new BattlefieldButcher(), new BattlefieldButcher(), new BattlefieldButcher()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already tapped");
        harness.assertLife(player2, 20);
    }

    @Test
    void summoningSickCreatureCannotActivate() {
        var butcher = harness.addToBattlefieldAndReturn(player1, new BattlefieldButcher());
        butcher.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
        harness.assertLife(player2, 20);
    }

    @Test
    void abilityResolvesAfterSourceLeavesAndGraveyardChanges() {
        addCreatureReady(player1, new BattlefieldButcher());
        harness.setGraveyard(player1, List.of(new BattlefieldButcher(), new BattlefieldButcher(),
                new BattlefieldButcher(), new BattlefieldButcher(), new BattlefieldButcher()));
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
