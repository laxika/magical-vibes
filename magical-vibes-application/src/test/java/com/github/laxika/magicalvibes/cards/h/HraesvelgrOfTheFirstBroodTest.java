package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({HraesvelgrOfTheFirstBrood.class, GrizzlyBears.class, Shock.class, Island.class})
class HraesvelgrOfTheFirstBroodTest extends BaseCardTest {

    @Test
    @DisplayName("When Hraesvelgr enters, target creature gets +1/+0 and can't be blocked")
    void enterTriggerBoostsAndMakesCreatureUnblockable() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HraesvelgrOfTheFirstBrood()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting a noncreature spell triggers Hraesvelgr's Shiva's Aid")
    void noncreatureSpellTriggersAbility() {
        harness.addToBattlefield(player1, new HraesvelgrOfTheFirstBrood());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Shiva's Aid")
    void creatureSpellDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new HraesvelgrOfTheFirstBrood());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Shiva's Aid wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HraesvelgrOfTheFirstBrood());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Shiva's Aid can target only creatures")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new HraesvelgrOfTheFirstBrood());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enterTriggerCanTargetHraesvelgrItself() {
        Permanent hraesvelgr = harness.enterBattlefieldAndReturn(player1, new HraesvelgrOfTheFirstBrood());

        harness.handlePermanentChosen(player1, hraesvelgr.getId());
        harness.passBothPriorities();

        assertThat(hraesvelgr.getPowerModifier()).isEqualTo(1);
        assertThat(hraesvelgr.getToughnessModifier()).isZero();
        assertThat(hraesvelgr.isCantBeBlocked()).isTrue();
    }

    @Test
    void canTargetItselfWithoutPayingItsOwnWard() {
        Permanent hraesvelgr = harness.addToBattlefieldAndReturn(player1, new HraesvelgrOfTheFirstBrood());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, hraesvelgr.getId());
        harness.passBothPriorities();

        assertThat(hraesvelgr.getPowerModifier()).isEqualTo(1);
        assertThat(hraesvelgr.getToughnessModifier()).isZero();
        assertThat(hraesvelgr.isCantBeBlocked()).isTrue();
    }

    @Test
    void eachNoncreatureSpellAddsAnotherBoost() {
        harness.addToBattlefield(player1, new HraesvelgrOfTheFirstBrood());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerAbility() {
        Permanent hraesvelgr = harness.addToBattlefieldAndReturn(player1, new HraesvelgrOfTheFirstBrood());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(hraesvelgr.getPowerModifier()).isZero();
        assertThat(hraesvelgr.isCantBeBlocked()).isFalse();
    }

    @Test
    void abilityDoesNotAffectAnotherCreatureWhenItsTargetDies() {
        Permanent hraesvelgr = harness.addToBattlefieldAndReturn(player1, new HraesvelgrOfTheFirstBrood());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(hraesvelgr.getPowerModifier()).isZero();
        assertThat(hraesvelgr.isCantBeBlocked()).isFalse();
    }
}
