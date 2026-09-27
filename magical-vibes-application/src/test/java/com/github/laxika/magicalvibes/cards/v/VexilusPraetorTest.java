package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexilusPraetor.class, GrizzlyBears.class, KrosanVerge.class})
class VexilusPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Your commanders have protection from everything")
    void protectsYourCommandersFromEverySource() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), commander.getCard());
        Permanent creatureSource = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent landSource = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThat(gqs.hasProtectionFromSource(gd, commander, creatureSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, commander, landSource)).isTrue();
    }

    @Test
    @DisplayName("Protects only commanders you control")
    void doesNotProtectNonCommandersOrOpponentsCommanders() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent ownCommander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), ownCommander.getCard());
        Permanent ownNonCommander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCommander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.makeCommander(player2.getId(), opponentCommander.getCard());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, ownCommander, source)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, ownNonCommander, source)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, opponentCommander, source)).isFalse();
    }
}
