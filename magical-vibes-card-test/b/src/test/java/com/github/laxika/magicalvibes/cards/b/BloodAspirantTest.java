package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.g.GravebreakerLamia;
import com.github.laxika.magicalvibes.cards.s.StampedeRider;
import com.github.laxika.magicalvibes.cards.u.UnderworldBreach;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodAspirant.class, GravebreakerLamia.class, StampedeRider.class,
        UnderworldBreach.class, FieldOfRuin.class})
class BloodAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature puts a +1/+1 counter on Blood Aspirant")
    void sacrificingCreaturePutsCounterOnBloodAspirant() {
        Permanent aspirant = addReadyAspirant(player1);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new StampedeRider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());

        activate(target);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Stampede Rider");
    }

    @Test
    @DisplayName("Sacrificing an enchantment puts a +1/+1 counter on Blood Aspirant")
    void sacrificingEnchantmentPutsCounterOnBloodAspirant() {
        Permanent aspirant = addReadyAspirant(player1);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new UnderworldBreach());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());

        activate(target);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Underworld Breach");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyAspirant(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnderworldBreach());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
    }

    private Permanent addReadyAspirant(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BloodAspirant());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Blood Aspirant can sacrifice itself and its ability still resolves")
    void canSacrificeItself() {
        Permanent aspirant = addReadyAspirant(player1);
        Permanent watcher = addReadyAspirant(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());

        activate(target);
        harness.handlePermanentChosen(player1, aspirant.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blood Aspirant");
        assertThat(watcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a land for another ability also triggers Blood Aspirant")
    void sacrificingLandTriggersCounter() {
        Permanent aspirant = addReadyAspirant(player1);
        harness.addToBattlefield(player1, new FieldOfRuin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Field of Ruin");
    }

    @Test
    @DisplayName("An opponent's sacrifice only triggers their own Blood Aspirant")
    void opponentSacrificeDoesNotTriggerCounter() {
        Permanent aspirant = addReadyAspirant(player1);
        Permanent opponentAspirant = addReadyAspirant(player2);
        harness.addToBattlefield(player2, new FieldOfRuin());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FieldOfRuin());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentAspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature that is also an enchantment generates only one sacrifice trigger")
    void enchantmentCreatureSacrificeTriggersOnlyOnce() {
        Permanent aspirant = addReadyAspirant(player1);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GravebreakerLamia());

        activate(aspirant);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(aspirant.getMarkedDamage()).isEqualTo(1);
        assertThat(aspirant.isCantBlockThisTurn()).isTrue();
        harness.assertOnBattlefield(player1, "Blood Aspirant");
    }

    @Test
    @DisplayName("A summoning-sick Blood Aspirant cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent aspirant = addReadyAspirant(player1);
        aspirant.setSummoningSick(true);
        harness.addToBattlefield(player1, new UnderworldBreach());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());

        assertThatThrownBy(() -> activate(target)).isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Underworld Breach");
        assertThat(aspirant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Blood Aspirant cannot activate its ability")
    void tappedSourceCannotActivate() {
        Permanent aspirant = addReadyAspirant(player1);
        aspirant.tap();
        harness.addToBattlefield(player1, new UnderworldBreach());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());

        assertThatThrownBy(() -> activate(target)).isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Underworld Breach");
    }

    @Test
    @DisplayName("The ability requires both generic and red mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent aspirant = addReadyAspirant(player1);
        harness.addToBattlefield(player1, new UnderworldBreach());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Underworld Breach");
        assertThat(aspirant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A land cannot pay the creature or enchantment sacrifice cost")
    void cannotSacrificeLandForActivatedAbility() {
        addReadyAspirant(player1);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new UnderworldBreach());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new FieldOfRuin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GravebreakerLamia());

        activate(target);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Field of Ruin");
        harness.assertInGraveyard(player1, "Underworld Breach");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }
}
