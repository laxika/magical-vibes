package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ChandrasIncinerator.class, AlpineWatchdog.class, Shock.class, ChandraHeartOfFire.class})
class ChandrasIncineratorTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less by the noncombat damage dealt to opponents this turn")
    void noncombatDamageReducesGenericCost() {
        gd.noncombatDamageDealtToPlayersThisTurn.put(player2.getId(), 5);
        harness.setHand(player1, List.of(new ChandrasIncinerator()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Still requires its red mana after the generic cost is reduced")
    void costReductionDoesNotReduceRedMana() {
        gd.noncombatDamageDealtToPlayersThisTurn.put(player2.getId(), 5);
        harness.setHand(player1, List.of(new ChandrasIncinerator()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Deals the triggering damage amount to a creature controlled by the damaged opponent")
    void damagesCreatureControlledByDamagedOpponent() {
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Does not trigger from noncombat damage dealt by an opponent's source")
    void doesNotTriggerFromOpponentSource() {
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        harness.addToBattlefield(player2, new AlpineWatchdog());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void partialReductionUsesDamageActuallyDealt() {
        harness.setHand(player1, List.of(new Shock(), new ChandrasIncinerator()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void damageToControllerDoesNotReduceCost() {
        gd.noncombatDamageDealtToPlayersThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new ChandrasIncinerator()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void damagesPlaneswalkerAndExcludesControllersPermanents() {
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        harness.addToBattlefield(player1, new AlpineWatchdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHeartOfFire());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void targetChangingControllerMakesTriggerIllegal() {
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void triggerStillResolvesAfterIncineratorLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ChandrasIncinerator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void noTriggerTargetWhenDamagedOpponentControlsNoEligiblePermanent() {
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void opponentsSourceDamagingThatOpponentDoesNotTrigger() {
        harness.addToBattlefield(player1, new ChandrasIncinerator());
        harness.addToBattlefield(player2, new AlpineWatchdog());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
