package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrowdedCrypt.class, GrizzlyBears.class, Shock.class})
class CrowdedCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana when tapped")
    void addsBlackMana() {
        Permanent crypt = addReadyCrypt();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(crypt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Gains a corpse counter whenever a creature you control dies")
    void gainsCorpseCounterWhenAllyCreatureDies() {
        Permanent crypt = addReadyCrypt();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        destroyWithShock(bears.getId());
        harness.passBothPriorities();

        assertThat(crypt.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates one decayed Zombie per corpse counter and sacrifices itself")
    void createsDecayedZombiesForCorpseCounters() {
        Permanent crypt = addReadyCrypt();
        crypt.setCounterCount(CounterType.CORPSE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crypt);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allMatch(permanent -> permanent.getCard().getKeywords().contains(Keyword.DECAYED));
    }

    private Permanent addReadyCrypt() {
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CrowdedCrypt());
        crypt.setSummoningSick(false);
        return crypt;
    }

    private void destroyWithShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }
}
