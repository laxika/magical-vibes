package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JibbirikOmnivore;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Boommobile")
@CardUsed({Boommobile.class, JibbirikOmnivore.class})
class BoommobileTest extends BaseCardTest {

    @Test
    @DisplayName("ETB adds four same-color ability-only mana, which pays for its exhaust ability")
    void etbManaPaysForExhaustAbility() {
        harness.setHand(player1, List.of(new Boommobile()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.RED)).isEqualTo(4);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.passBothPriorities();

        Permanent boommobile = findPermanent(player1, "Boommobile");
        harness.assertLife(player2, 19);
        assertThat(boommobile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new Boommobile());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void zeroDamageStillAddsCounterWithoutCrewing() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new Boommobile());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void exhaustCanDestroyAnOpposingCreature() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new Boommobile());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JibbirikOmnivore());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jibbirik Omnivore");
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void exhaustCannotTargetANoncreatureVehicle() {
        harness.addToBattlefield(player1, new Boommobile());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Boommobile());
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCanCrewAndCounterIncreasesAnimatedPower() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new Boommobile());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new JibbirikOmnivore());
        pilot.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);
    }

    @Test
    void entryManaCannotCastAnotherVehicle() {
        harness.setHand(player1, List.of(new Boommobile(), new Boommobile()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.RED)).isEqualTo(4);
        assertThat(countPermanents(player1, "Boommobile")).isEqualTo(1);
    }

    @Test
    void nonRedEntryManaCanPayGenericAndXCosts() {
        harness.setHand(player1, List.of(new Boommobile()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(findPermanent(player1, "Boommobile").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.GREEN)).isZero();
    }

    @Test
    void entryManaMayBeSplitBetweenDifferentExhaustAbilities() {
        harness.addToBattlefield(player1, new Boommobile());
        harness.setHand(player1, List.of(new Boommobile()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Boommobile"))
                .allSatisfy(vehicle -> assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.RED)).isZero();
        harness.assertLife(player2, 20);
    }
}
