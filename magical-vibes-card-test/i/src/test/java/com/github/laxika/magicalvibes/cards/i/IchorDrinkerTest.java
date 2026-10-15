package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IchorDrinker.class})
class IchorDrinkerTest extends BaseCardTest {

    @Test
    void graveyardAbilityIncubatesTwoAndExilesIchorDrinker() {
        IchorDrinker card = new IchorDrinker();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Ichor Drinker");
    }

    @Test
    void graveyardAbilityCanOnlyBeActivatedAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new IchorDrinker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exileIsPaidBeforeIncubateResolves() {
        IchorDrinker card = new IchorDrinker();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Ichor Drinker");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(card.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Incubator");

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
    }

    @Test
    void incubatorTransformsAtInstantSpeedAndKeepsItsCounters() {
        harness.setGraveyard(player1, List.of(new IchorDrinker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
    }

    @Test
    void graveyardAbilityCannotBeActivatedOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new IchorDrinker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Ichor Drinker");
    }

    @Test
    void graveyardAbilityCannotBeActivatedWithAnAbilityOnTheStack() {
        harness.setGraveyard(player1, List.of(new IchorDrinker(), new IchorDrinker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
    }

    @Test
    void graveyardAbilityRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new IchorDrinker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Ichor Drinker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void combatDamageGainsLifeThroughLifelink() {
        addCreatureReady(player1, new IchorDrinker());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
