package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.h.HoneyMammoth;
import com.github.laxika.magicalvibes.cards.t.TitanothRex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamThrough.class, TitanothRex.class, DrannithHealer.class, HoneyMammoth.class})
class RamThroughTest extends BaseCardTest {

    @Test
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
    void rejectsCreatureYouControlAsTheSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HoneyMammoth());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
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
    void rejectsOpponentsCreatureAsTheDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new TitanothRex());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrannithHealer());
        harness.setHand(player1, List.of(new RamThrough()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
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

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
