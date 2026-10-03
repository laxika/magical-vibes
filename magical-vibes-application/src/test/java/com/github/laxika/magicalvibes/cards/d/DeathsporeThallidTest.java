package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DeathsporeThallid.class, AshcoatBear.class, Forest.class,
        ArtificialEvolution.class, Bitterblossom.class})
class DeathsporeThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    @DisplayName("Removing three spore counters leaves additional counters")
    void removesExactlyThreeSporeCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isOne();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Saproling gives a creature -1/-1")
    void sacrificingSaprolingDebuffsTargetCreature() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The debuff puts a creature with zero toughness into the graveyard")
    void debuffPutsZeroToughnessCreatureIntoGraveyard() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new DeathsporeThallid());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Deathspore Thallid")).isEmpty();
        harness.assertInGraveyard(player2, "Deathspore Thallid");
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addThallid().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The debuff ability cannot sacrifice a non-Saproling creature")
    void debuffAbilityRequiresSaproling() {
        addThallid();
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        addCreatureReady(player1, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The debuff ability requires a creature target")
    void debuffAbilityRequiresCreatureTarget() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The debuff wears off during cleanup")
    void debuffWearsOffAtEndOfTurn() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("No spore counter is added during an opponent's upkeep")
    void opponentUpkeepDoesNotAddSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Counters are paid immediately and a tapped summoning-sick Thallid can activate")
    void tokenAbilityPaysCountersBeforeResolutionWithoutTapRestriction() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new DeathsporeThallid());
        thallid.setSummoningSick(true);
        thallid.setTapped(true);
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(thallid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The targeted Saproling may itself pay the sacrifice cost")
    void canSacrificeTargetedSaproling() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Saproling");

        harness.activateAbility(player1, 0, 1, null, token.getId());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Deathspore Thallid");
    }

    @Test
    @DisplayName("The token ability resolves after its source dies")
    void tokenAbilityResolvesAfterSourceDies() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, thallid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deathspore Thallid");
        harness.assertInGraveyard(player1, "Deathspore Thallid");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature Saproling permanent can pay the sacrifice cost")
    void canSacrificeNoncreatureSaproling() {
        addThallid();
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, blossom.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SAPROLING");

        assertThat(gqs.hasEffectiveSubtype(gd, blossom, CardSubtype.SAPROLING)).isTrue();
        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Bitterblossom");
        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private Permanent addThallid() {
        return addCreatureReady(player1, new DeathsporeThallid());
    }
}
