package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmokeTellerTest extends BaseCardTest {

    @Test
    void looksAtTargetFaceDownCreatureOnlyForItsController() {
        addCreatureReady(player1, new SmokeTeller());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, faceDownCreature.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .anyMatch(message -> message.contains("Master of Pearls"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(gd.gameLog.stream().map(log -> log.plainText()))
                .anyMatch(log -> log.contains("looks at a face-down creature"))
                .noneMatch(log -> log.contains("Master of Pearls"));
    }

    @Test
    void cannotTargetFaceUpCreature() {
        addCreatureReady(player1, new SmokeTeller());
        Permanent faceUpCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceUpCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
