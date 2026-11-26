package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.b.BasiliskCollar;
import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.h.HoneyMammoth;
import com.github.laxika.magicalvibes.cards.t.TitanothRex;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamThrough.class, AjanisPridemate.class, BasiliskCollar.class, TitanothRex.class, DrannithHealer.class, HoneyMammoth.class})
class RamThroughTest extends BaseCardTest {

    @Test
    @DisplayName("A trampler deals lethal damage to the creature and the excess to its controller")
    void trampleDealsExcessDamageToTargetCreatureController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertInGraveyard(player2, "Drannith Healer");
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Without trample all the damage is dealt to the creature")
    void withoutTrampleDoesNotDealExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HoneyMammoth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertInGraveyard(player2, "Drannith Healer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Rejects a creature you control as the creature dealt damage")
    void rejectsCreatureYouControlAsTheSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HoneyMammoth());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The excess is dealt to the controller instead of to the creature; the dealer is dealt nothing")
    void trampleRedirectsExcessRatherThanAlsoDealingItToTheCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HoneyMammoth());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertLife(player2, 15);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Honey Mammoth");
    }

    @Test
    @DisplayName("Damage already marked lowers the lethal damage before the excess is worked out")
    void alreadyMarkedDamageReducesDamageNeededBeforeExcessIsRedirected() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HoneyMammoth());
        target.setMarkedDamage(2);
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Rejects an opponent's creature as the dealer")
    void rejectsOpponentsCreatureAsTheDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A dealer that left before resolution deals no damage")
    void noDamageWhenSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Drannith Healer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With the creature gone before resolution its controller is dealt nothing")
    void noPlayerDamageWhenVictimLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A lifelink deathtouch trampler deals 1 to the creature and the rest to its controller, gaining life once")
    void lifelinkDeathtouchTramplerDealsOneEvent() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        Permanent collar = harness.addToBattlefieldAndReturn(player1, new BasiliskCollar());
        collar.setAttachedTo(source.getId());
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Drannith Healer");
        harness.assertLife(player2, 10);
        harness.assertLife(player1, 31);
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
