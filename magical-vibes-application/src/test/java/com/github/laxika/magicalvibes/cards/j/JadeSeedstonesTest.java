package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JadeSeedstones.class, JadeheartAttendant.class, PanickedAltisaur.class})
class JadeSeedstonesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes three +1/+1 counters among creatures you control")
    void distributesCountersAmongControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 2);

        castSeedstones();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Craft returns Jadeheart Attendant and gains life equal to the craft material's mana value")
    void craftsAndGainsLifeEqualToMaterialManaValue() {
        harness.addToBattlefield(player1, new JadeSeedstones());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent attendant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof JadeheartAttendant)
                .findFirst().orElseThrow();
        assertThat(attendant.isTransformed()).isTrue();
        harness.assertLife(player1, 15);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
    }

    @Test
    void distributesOneCounterToEachOfThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 1, third.getId(), 1);

        castSeedstones();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canChooseOnlyOneCreatureAndCannotTargetOpponentsCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 3);

        castSeedstones();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(first.getId(), second.getId())
                .doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canEnterWithoutAnyCreaturesToTarget() {
        castSeedstones();

        harness.assertOnBattlefield(player1, "Jade Seedstones");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftsWithCreatureCardFromGraveyardAndLifeGainUsesTheStack() {
        harness.addToBattlefield(player1, new JadeSeedstones());
        PanickedAltisaur material = new PanickedAltisaur();
        harness.setGraveyard(player1, List.of(material));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Jade Seedstones");
        harness.assertNotInGraveyard(player1, "Panicked Altisaur");
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Jadeheart Attendant");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 15);
    }

    @Test
    void cannotCraftOutsideMainPhase() {
        harness.addToBattlefield(player1, new JadeSeedstones());
        harness.setGraveyard(player1, List.of(new PanickedAltisaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Jade Seedstones");
        harness.assertInGraveyard(player1, "Panicked Altisaur");
    }

    private void castSeedstones() {
        harness.setHand(player1, List.of(new JadeSeedstones()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
