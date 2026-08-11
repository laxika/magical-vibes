package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevotedCaretakerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants protection from instants and sorceries to any permanent you control")
    void grantsProtectionToAnyPermanentYouControl() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Plains");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent plains = findPermanent(player1, "Plains");
        assertThat(plains.getProtectionFromCardTypes())
                .contains(CardType.INSTANT, CardType.SORCERY);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(plains.getProtectionFromCardTypes()).doesNotContain(CardType.INSTANT, CardType.SORCERY);
    }

    @Test
    @DisplayName("Protection from instants and sorceries prevents those spells from targeting the permanent")
    void protectionPreventsInstantAndSorceryTargeting() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock(), new PillarOfFlame()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castSorcery(player2, 1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The ability cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        addCreatureReady(player1, new DevotedCaretaker());
        harness.addToBattlefield(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentPermanentId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
