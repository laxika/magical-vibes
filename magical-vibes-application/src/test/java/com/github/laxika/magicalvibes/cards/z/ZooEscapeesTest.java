package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZooEscapees.class, GrizzlyBears.class, LightningBolt.class})
class ZooEscapeesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Mutagen artifact token when it leaves the battlefield")
    void createsMutagenWhenLeavingBattlefield() {
        createMutagenToken();

        harness.assertInGraveyard(player1, "Zoo Escapees");
        Permanent mutagen = findPermanent(player1, "Mutagen");
        assertThat(mutagen.getCard().isToken()).isTrue();
        assertThat(mutagen.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("The created Mutagen token can put a +1/+1 counter on a creature")
    void mutagenPutsCounterOnCreature() {
        createMutagenToken();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0,
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void createMutagenToken() {
        harness.addToBattlefield(player1, new ZooEscapees());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Zoo Escapees"));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
