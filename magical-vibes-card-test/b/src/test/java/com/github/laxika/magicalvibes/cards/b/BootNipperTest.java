package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.cards.h.HoneyMammoth;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BootNipper.class, HoneyMammoth.class})
class BootNipperTest extends BaseCardTest {

    @Test
    void entersWithDeathtouchCounterWhenChosen() {
        Permanent nipper = castAndChoose("deathtouch");

        assertThat(nipper.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(nipper.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(nipper.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(nipper.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    void entersWithLifelinkCounterWhenChosen() {
        Permanent nipper = castAndChoose("lifelink");

        assertThat(nipper.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(nipper.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(nipper.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(nipper.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void deathtouchCounterMakesCombatDamageLethalToLargerBlocker() {
        Permanent nipper = castAndChoose("deathtouch");
        addCreatureReady(player2, new HoneyMammoth());
        nipper.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Honey Mammoth");
        harness.assertNotOnBattlefield(player1, "Boot Nipper");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifelinkCounterGainsLifeWithoutMakingDamageToLargerBlockerLethal() {
        Permanent nipper = castAndChoose("lifelink");
        Permanent blocker = addCreatureReady(player2, new HoneyMammoth());
        nipper.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Honey Mammoth");
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Boot Nipper");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    private Permanent castAndChoose(String counterType) {
        harness.castFromHand(player1, new BootNipper(), "{1}{B}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("deathtouch", "lifelink");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Boot Nipper");
    }
}
