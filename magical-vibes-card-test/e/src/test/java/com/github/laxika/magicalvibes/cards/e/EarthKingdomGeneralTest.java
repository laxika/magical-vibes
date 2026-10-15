package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthKingdomGeneral.class, BurstOfStrength.class, Forest.class})
class EarthKingdomGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by earthbending a land you control")
    void earthbendsLandOnEntry() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthKingdomGeneral()));
        addMana(player1, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Declining the counter trigger leaves it available later that turn")
    void decliningDoesNotUseOncePerTurnTrigger() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());

        castBurstOfStrength(general);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 20);

        castBurstOfStrength(general);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Accepting the counter trigger prevents another one that turn")
    void acceptingUsesOncePerTurnTrigger() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());

        castBurstOfStrength(general);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);

        castBurstOfStrength(general);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot earthbend a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EarthKingdomGeneral()));
        addMana(player1, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownLand.getId())
                .doesNotContain(opponentLand.getId());
    }

    @Test
    @DisplayName("Your counters on an opposing creature allow you to gain life")
    void yourCountersOnOpposingCreatureTriggerLifeGain() {
        harness.addToBattlefield(player1, new EarthKingdomGeneral());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new EarthKingdomGeneral());

        castBurstOfStrength(opposingCreature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent putting counters on your creature does not trigger your General")
    void opponentsCountersDoNotTriggerLifeGain() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, general.getId());

        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The life gain is available again on the opponent's turn")
    void lifeGainResetsOnNextTurn() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());
        castBurstOfStrength(general);
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        castBurstOfStrength(general);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Multiple pending counter triggers still allow life gain only once")
    void pendingTriggersShareOncePerTurnLimit() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new EarthKingdomGeneral());
        harness.setHand(player1, List.of(new BurstOfStrength(), new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, general.getId());
        harness.castAndResolveInstant(player1, 0, general.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An earthbent land returns tapped without its animation or counters after dying")
    void earthbentLandReturnsAfterDeath() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthKingdomGeneral()));
        addMana(player1, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        land.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Forest");
        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returnedLand)).isTrue();
        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
        assertThat(returnedLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBurstOfStrength(Permanent target) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, int colorless) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, colorless);
    }
}
