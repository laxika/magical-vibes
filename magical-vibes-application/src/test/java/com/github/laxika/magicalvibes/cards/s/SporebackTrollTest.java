package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.n.NovijenHeartOfProgress;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
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

@CardUsed({SporebackTroll.class, MistralCharger.class, NovijenHeartOfProgress.class, WreckingBall.class})
class SporebackTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent troll = castTroll();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Graft moves a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent troll = castTroll();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent troll = castTroll();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, false);

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller may graft onto an opponent's entering creature")
    void graftMovesCounterOntoOpponentsCreature() {
        Permanent troll = castTroll();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player2, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerates a target creature with a +1/+1 counter")
    void regeneratesTargetCreatureWithCounter() {
        castTroll();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, charger.getId());
        harness.passBothPriorities();

        assertThat(charger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shields an eligible creature from destruction")
    void regenerationShieldSavesTargetFromDestruction() {
        castTroll();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, charger.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, charger.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(charger);
        assertThat(charger.getRegenerationShield()).isZero();
        assertThat(charger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot regenerate a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        castTroll();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, charger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with a +1/+1 counter");
    }

    @Test
    @DisplayName("Cannot target a noncreature even if it has a +1/+1 counter")
    void cannotTargetNoncreatureWithCounter() {
        castTroll();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NovijenHeartOfProgress());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with a +1/+1 counter");
    }

    @Test
    @DisplayName("Cannot target a creature that loses its +1/+1 counter before resolution")
    void targetMustStillHaveCounterOnResolution() {
        castTroll();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, charger.getId());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(charger.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Can regenerate an opponent's creature with a +1/+1 counter")
    void canRegenerateOpponentsCreature() {
        Permanent troll = castTroll();
        Permanent charger = addCreatureReady(player2, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, charger.getId());
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isZero();
        assertThat(charger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Moving the last graft counter kills the Troll but keeps the moved counter")
    void movingLastCounterKillsTroll() {
        Permanent troll = castTroll();
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Sporeback Troll");
        harness.assertInGraveyard(player1, "Sporeback Troll");
    }

    @Test
    @DisplayName("Graft does not move a counter if its source leaves before resolution")
    void graftDoesNothingWhenSourceLeaves() {
        Permanent troll = castTroll();
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, troll.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sporeback Troll");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Regeneration resolves even if the Troll leaves the battlefield")
    void regenerationResolvesAfterSourceLeaves() {
        Permanent troll = castTroll();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, charger.getId());

        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, troll.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sporeback Troll");
        assertThat(charger.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent castTroll() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SporebackTroll(), "{3}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Sporeback Troll");
    }
}
