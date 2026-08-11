package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscipleOfGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from black prevents black spells from targeting Disciple of Grace")
    void protectionFromBlackPreventsBlackSpellTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfGrace());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Disciple of Grace and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DiscipleOfGrace()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Grace");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
