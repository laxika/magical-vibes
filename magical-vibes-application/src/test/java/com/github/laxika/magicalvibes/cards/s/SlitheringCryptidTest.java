package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlitheringCryptid.class, GrizzlyBears.class})
class SlitheringCryptidTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Mutagen artifact token when it enters the battlefield")
    void createsMutagenWhenEnteringBattlefield() {
        harness.enterBattlefieldAndReturn(player1, new SlitheringCryptid());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mutagen")).isNotNull();
    }

    @Test
    @DisplayName("The Mutagen token puts a +1/+1 counter on a target creature")
    void mutagenPutsCounterOnCreature() {
        harness.enterBattlefieldAndReturn(player1, new SlitheringCryptid());
        harness.passBothPriorities();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0,
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
