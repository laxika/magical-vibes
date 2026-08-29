package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CacophodonTest extends BaseCardTest {

    @Test
    void damageTriggersUntapOfTargetPermanent() {
        harness.addToBattlefield(player2, new Cacophodon());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID cacoId = harness.getPermanentId(player2, "Cacophodon");
        harness.castInstant(player1, 0, cacoId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }
}
