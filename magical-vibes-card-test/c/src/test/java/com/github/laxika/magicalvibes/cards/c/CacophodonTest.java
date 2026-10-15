package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cacophodon.class, Forest.class, Shock.class})
class CacophodonTest extends BaseCardTest {

    @Test
    void damageTriggersUntapOfTargetPermanent() {
        Permanent cacophodon = harness.addToBattlefieldAndReturn(player2, new Cacophodon());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cacophodon.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void damageCanUntapOpponentsPermanent() {
        Permanent cacophodon = harness.addToBattlefieldAndReturn(player2, new Cacophodon());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cacophodon.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, forest.getId());
        assertThat(forest.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void damageCanUntapCacophodonItself() {
        Permanent cacophodon = harness.addToBattlefieldAndReturn(player2, new Cacophodon());
        cacophodon.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cacophodon.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, cacophodon.getId());
        harness.passBothPriorities();

        assertThat(cacophodon.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Cacophodon");
    }

    @Test
    void untappedPermanentIsALegalTarget() {
        Permanent cacophodon = harness.addToBattlefieldAndReturn(player2, new Cacophodon());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cacophodon.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachDamageEventTriggersEvenWhenTheLastOneIsLethal() {
        Permanent cacophodon = harness.addToBattlefieldAndReturn(player2, new Cacophodon());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int damageEvent = 0; damageEvent < 3; damageEvent++) {
            forest.tap();
            harness.castInstant(player1, 0, cacophodon.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            if (damageEvent == 2) {
                harness.assertNotOnBattlefield(player2, "Cacophodon");
                harness.assertInGraveyard(player2, "Cacophodon");
            }
            harness.handlePermanentChosen(player2, forest.getId());
            harness.passBothPriorities();

            assertThat(forest.isTapped()).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }
}
