package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenEternal.class, PollenbrightDruid.class, DoublingSeason.class})
class AvenEternalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB amasses Zombies 1 by creating a 0/0 Zombie Army with a counter")
    void amassesWithoutAnArmy() {
        castAvenEternal();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCard().getName()).isEqualTo("Zombie Army");
        assertThat(army.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, army)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, army)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB amasses Zombies 1 on an existing Army and makes it a Zombie")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);

        castAvenEternal();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.subtypesOf(gd, army)).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Amass chooses only one of multiple Armies and preserves its other subtypes")
    void choosesOneArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        PermanentGrants.subtype(gd, first, CardSubtype.ARMY);
        PermanentGrants.subtype(gd, second, CardSubtype.ARMY);

        castAvenEternal();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(PermanentGrants.subtypesOf(gd, first)).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.subtypesOf(gd, second)).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.ELF, CardSubtype.DRUID, CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's Army does not prevent creating your own Army")
    void ignoresOpponentsArmy() {
        Permanent opponentArmy = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        PermanentGrants.subtype(gd, opponentArmy, CardSubtype.ARMY);

        castAvenEternal();

        assertThat(opponentArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(PermanentGrants.subtypesOf(gd, opponentArmy)).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Doubling Season creates two Armies but amass puts counters on only the chosen one")
    void doubledTokensRequireChoosingOneArmy() {
        harness.addToBattlefield(player1, new DoublingSeason());

        castAvenEternal();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        List<Permanent> armies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(armies).hasSize(2);
        assertThat(armies).allSatisfy(army ->
                assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        harness.handleMultiplePermanentsChosen(player1, List.of(armies.getFirst().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> {
                    assertThat(army.getId()).isEqualTo(armies.getFirst().getId());
                    assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Amass checks for an Army when the trigger resolves")
    void createsArmyWhenPreviousArmyLeavesBeforeResolution() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);
        harness.castFromHand(player1, new AvenEternal(), "{2}{U}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(army);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(created -> assertThat(created.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    private void castAvenEternal() {
        harness.castFromHand(player1, new AvenEternal(), "{2}{U}");
        resolveAllTriggers();
    }
}
