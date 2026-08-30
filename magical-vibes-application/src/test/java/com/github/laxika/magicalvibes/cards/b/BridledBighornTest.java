package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BridledBighorn.class, GrizzlyBears.class})
class BridledBighornTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Bridled Bighorn")
    void saddleTapsAnotherCreature() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bighorn.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled creates a Sheep token")
    void attacksWhileSaddledCreatesSheep() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());
        bighorn.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not create a Sheep token")
    void doesNotCreateSheepWhenNotSaddled() {
        addCreatureReady(player1, new BridledBighorn());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent bighorn = addCreatureReady(player1, new BridledBighorn());

        declareAttackers(player1, List.of(0));
        bighorn.setSaddled(true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sheep")).isEmpty();
    }
}
