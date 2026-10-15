package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenSoulgazer.class, AscendingAven.class})
class AvenSoulgazerTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at a target face-down creature only for its controller")
    void looksAtTargetFaceDownCreatureOnlyForItsController() {
        addCreatureReady(player1, new AvenSoulgazer());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new AscendingAven());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, faceDownCreature.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .anyMatch(message -> message.contains("Ascending Aven"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(faceDownCreature.isFaceDown()).isTrue();
        assertThat(gd.gameLog.stream().map(log -> log.plainText()))
                .anyMatch(log -> log.contains("looks at a face-down creature"))
                .noneMatch(log -> log.contains("Ascending Aven"));
    }

    @Test
    @DisplayName("Cannot target a face-up creature")
    void cannotTargetFaceUpCreature() {
        addCreatureReady(player1, new AvenSoulgazer());
        Permanent faceUpCreature = addCreatureReady(player2, new AscendingAven());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceUpCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a face-down noncreature permanent")
    void cannotTargetFaceDownNoncreaturePermanent() {
        addCreatureReady(player1, new AvenSoulgazer());
        Permanent faceDownNoncreature = harness.addToBattlefieldAndReturn(player2, new AscendingAven());
        faceDownNoncreature.setFaceDown(2, 2, Set.of(CardType.LAND));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceDownNoncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two generic mana and one white mana")
    void requiresTwoGenericAndOneWhiteMana() {
        addCreatureReady(player1, new AvenSoulgazer());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new AscendingAven());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceDownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not look at a creature that turns face up in response")
    void doesNotLookAtCreatureTurnedFaceUpInResponse() {
        addCreatureReady(player1, new AvenSoulgazer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AscendingAven());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.turnFaceUp(player2, 0);
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(gd.gameLog.stream().map(log -> log.plainText()))
                .noneMatch(log -> log.contains("looks at a face-down creature"));
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick to look at its own creature")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent soulgazer = harness.addToBattlefieldAndReturn(player1, new AvenSoulgazer());
        soulgazer.tap();
        soulgazer.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AscendingAven());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .anyMatch(message -> message.contains("Ascending Aven"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(target.isFaceDown()).isTrue();
        assertThat(soulgazer.isTapped()).isTrue();
    }
}
