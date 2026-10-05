package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LazotepPlating.class, GrizzlyBears.class, Island.class, Shock.class,
        PollenbrightDruid.class, DoublingSeason.class})
class LazotepPlatingTest extends BaseCardTest {

    @Test
    @DisplayName("Amasses Zombies 1 and gives the controller and existing permanents hexproof")
    void amassesAndProtectsControllerAndPermanents() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        castLazotepPlating();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, island, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Amasses on an existing Army and protection wears off at end of turn")
    void amassesOnExistingArmyAndProtectionExpires() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepPlating();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Amass chooses one Army, preserves its subtypes, and then protects all your permanents")
    void choosesOneOfMultipleArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepPlating();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.ELF, CardSubtype.DRUID, CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Choosing an Army is mandatory when multiple Armies exist")
    void cannotDeclineArmyChoice() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepPlating();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Doubling Season creates two Armies but counters go on only the chosen Army")
    void doubledTokensRequireChoosingOneArmy() {
        harness.addToBattlefield(player1, new DoublingSeason());

        castLazotepPlating();

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
                    assertThat(gqs.hasKeyword(gd, army, Keyword.HEXPROOF)).isTrue();
                });
    }

    @Test
    @DisplayName("Opponent Armies are ignored and permanents entering later are not protected")
    void onlyProtectsOwnPermanentsPresentAtResolution() {
        Permanent opponentArmy = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        opponentArmy.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepPlating();
        Permanent laterPermanent = harness.addToBattlefieldAndReturn(player1, new PollenbrightDruid());

        assertThat(opponentArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, opponentArmy, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, laterPermanent, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Hexproof invalidates an opponent's spell already on the stack")
    void protectsAgainstSpellAlreadyOnStack() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.castFromHand(player1, new LazotepPlating(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("The controller may still target their own protected player and permanents")
    void permitsControllerTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castLazotepPlating();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    private void castLazotepPlating() {
        harness.castFromHand(player1, new LazotepPlating(), "{1}{U}");
        harness.passBothPriorities();
    }
}
