package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KavaronMemorialWorld.class, Forest.class, GrizzlyBears.class})
class KavaronMemorialWorldTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new KavaronMemorialWorld()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Kavaron, Memorial World").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds red mana")
    void tapAbilityAddsRedMana() {
        Permanent world = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());

        harness.activateAbility(player1, battlefieldIndex(world), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent world = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(world), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(world.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("At twelve charge counters, it creates a Robot and boosts only its controller's creatures")
    void twelveCountersCreateRobotAndBoostOwnCreatures() {
        Permanent world = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        world.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(world), 2, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The Robot ability requires twelve charge counters")
    void robotAbilityRequiresTwelveChargeCounters() {
        Permanent world = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        harness.addToBattlefield(player1, new Forest());
        world.setCounterCount(CounterType.CHARGE, 11);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(world), 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("charge counters");
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void boostAndHasteWearOffAtEndOfTurn() {
        Permanent world = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        world.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(world), 2, null, null);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
