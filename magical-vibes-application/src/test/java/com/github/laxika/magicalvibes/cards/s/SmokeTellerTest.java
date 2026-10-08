package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmokeTeller.class, MasterOfPearls.class})
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

        assertThat(faceDownCreature.isFaceDown()).isTrue();
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
        Permanent faceUpCreature = addCreatureReady(player2, new MasterOfPearls());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceUpCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent teller = harness.addToBattlefieldAndReturn(player1, new SmokeTeller());
        teller.setSummoningSick(true);
        teller.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .anyMatch(message -> message.contains("Master of Pearls"));
        assertThat(teller.isTapped()).isTrue();
        assertThat(target.isFaceDown()).isTrue();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new SmokeTeller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneBlueMana() {
        addCreatureReady(player1, new SmokeTeller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canLookAtOwnFaceDownCreature() {
        addCreatureReady(player1, new SmokeTeller());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MasterOfPearls());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .anyMatch(message -> message.contains("Master of Pearls"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(target.isFaceDown()).isTrue();
    }

    @Test
    void doesNotLookWhenTargetTurnsFaceUpInResponse() {
        addCreatureReady(player1, new SmokeTeller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passPriority(player1);
        harness.turnFaceUp(player2, 0);
        harness.clearMessages();
        resolveAllTriggers();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(gameLogContains("looks at a face-down creature")).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
