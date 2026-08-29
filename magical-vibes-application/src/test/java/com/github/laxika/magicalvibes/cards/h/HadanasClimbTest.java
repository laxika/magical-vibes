package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HadanasClimbTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Puts a counter on the target creature and transforms at three counters")
    void transformsWhenTargetReachesThreeCounters() {
        Permanent climb = harness.addToBattlefieldAndReturn(player1, new HadanasClimb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(climb.isTransformed()).isTrue();
        assertThat(climb.getCard().getName()).isEqualTo("Winged Temple of Orazca");
    }

    @Test
    @DisplayName("Puts a counter on the target creature without transforming below three counters")
    void doesNotTransformBelowThreshold() {
        Permanent climb = harness.addToBattlefieldAndReturn(player1, new HadanasClimb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(climb.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        Permanent climb = harness.addToBattlefieldAndReturn(player1, new HadanasClimb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(climb.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Winged Temple of Orazca adds one mana of the chosen color")
    void wingedTempleAddsAnyColorMana() {
        Permanent temple = addTransformedTemple(player1);

        harness.activateAbility(player1, indexOf(player1, temple), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Winged Temple of Orazca gives a controlled creature flying and power-sized pump")
    void wingedTempleBoostsTargetCreature() {
        Permanent temple = addTransformedTemple(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, temple), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Winged Temple of Orazca cannot target an opponent's creature")
    void wingedTempleCannotTargetOpponentCreature() {
        Permanent temple = addTransformedTemple(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, temple), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedTemple(Player player) {
        HadanasClimb card = new HadanasClimb();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
