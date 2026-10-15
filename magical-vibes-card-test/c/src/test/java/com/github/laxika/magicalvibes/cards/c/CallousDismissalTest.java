package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallousDismissal.class, GrizzlyBears.class, Island.class, PollenbrightDruid.class,
        DoublingSeason.class})
class CallousDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent and amasses Zombies 1 without an Army")
    void returnsPermanentAndAmassesWithoutAnArmy() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castCallousDismissal(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCard().getName()).isEqualTo("Zombie Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a nonland permanent and amasses Zombies 1 on an existing Army")
    void returnsPermanentAndAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castCallousDismissal(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new CallousDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void addsCounterToArmyCreatedByAnEarlierResolution() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        castCallousDismissal(firstTarget.getId());
        Permanent army = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());

        castCallousDismissal(secondTarget.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player2, "Pollenbright Druid");
    }

    @Test
    void returningOwnOnlyArmyCreatesANewArmyAfterTheBounce() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        castCallousDismissal(target.getId());
        Permanent oldArmy = gd.playerBattlefields.get(player1.getId()).getFirst();

        castCallousDismissal(oldArmy.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1).doesNotContain(oldArmy);
        Permanent newArmy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(newArmy.getCard().isToken()).isTrue();
        assertThat(newArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotAmassWhenTheOnlyTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());
        harness.setHand(player1, List.of(new CallousDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Callous Dismissal");
        harness.assertInGraveyard(player2, "Pollenbright Druid");
        harness.assertNotInHand(player2, "Pollenbright Druid");
    }

    @Test
    void doublingSeasonPutsCountersOnOnlyOneOfTheTwoCreatedArmies() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PollenbrightDruid());

        castCallousDismissal(target.getId());

        List<Permanent> armies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(armies).hasSize(2);
        assertThat(armies).allSatisfy(army ->
                assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        Permanent chosen = armies.getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .containsExactly(chosen);
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player2, "Pollenbright Druid");
    }

    @Test
    void returnsANoncreaturePermanentAndStillAmasses() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DoublingSeason());

        castCallousDismissal(target.getId());

        harness.assertNotOnBattlefield(player2, "Doubling Season");
        harness.assertInHand(player2, "Doubling Season");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent army = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(army.getCard().isToken()).isTrue();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castCallousDismissal(UUID targetId) {
        harness.setHand(player1, List.of(new CallousDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
