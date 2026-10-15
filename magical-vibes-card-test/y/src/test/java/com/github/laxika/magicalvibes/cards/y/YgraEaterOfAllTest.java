package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.cards.f.Fell;
import com.github.laxika.magicalvibes.cards.s.Savor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YgraEaterOfAll.class, GrizzlyBears.class, DaggerfangDuo.class, Fell.class, Savor.class})
class YgraEaterOfAllTest extends BaseCardTest {

    @Test
    void otherCreaturesBecomeFoodArtifacts() {
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.isArtifact(gd, ally)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ally, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.isArtifact(gd, opponent)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponent, CardSubtype.FOOD)).isTrue();
    }

    @Test
    void sacrificingAFoodGainsLifeAndPutsCountersOnYgra() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(ygra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void ygraDoesNotMakeItselfAFoodArtifact() {
        Permanent ygra = addCreatureReady(player1, new YgraEaterOfAll());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.isArtifact(gd, ygra)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ygra, CardSubtype.FOOD)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ygra, Eater of All");
    }

    @Test
    void opponentCanSacrificeGrantedFoodForLifeAndYgraGetsCounters() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        addCreatureReady(player2, new DaggerfangDuo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Daggerfang Duo");
        assertThat(ygra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void destroyedCreatureStillCountsAsFoodAtDeath() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Daggerfang Duo");
        assertThat(ygra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void sacrificingNoncreatureFoodTokenAlsoPutsCountersOnYgra() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        harness.setHand(player1, List.of(new Savor()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, ygra.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(ygra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentRemovalWhenTheyHaveNoFood() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Fell()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, ygra.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ygra, Eater of All");
        harness.assertInGraveyard(player2, "Fell");
    }

    @Test
    void wardCanBePaidWithOpponentCreatureAndTriggersYgra() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        Permanent food = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Savor()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, ygra.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, food.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Daggerfang Duo");
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Food");
        assertThat(ygra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void ygraDoesNotGrantFoodAbilityWhileItsCreatureSpellIsOnStack() {
        addCreatureReady(player1, new DaggerfangDuo());
        harness.setHand(player1, List.of(new YgraEaterOfAll()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Daggerfang Duo");
        harness.assertLife(player1, 20);
    }

    @Test
    void grantedTapAbilityCannotBeUsedBySummoningSickCreature() {
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        harness.addToBattlefield(player1, new DaggerfangDuo());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Daggerfang Duo");
        harness.assertLife(player1, 20);
    }

    @Test
    void foodConversionAndGrantedAbilityEndWhenYgraLeaves() {
        Permanent ygra = harness.addToBattlefieldAndReturn(player1, new YgraEaterOfAll());
        Permanent creature = addCreatureReady(player1, new DaggerfangDuo());
        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, ygra.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.isArtifact(gd, creature)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.FOOD)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Daggerfang Duo");
    }
}
