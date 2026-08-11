package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscipleOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from red prevents red spells from targeting Disciple of Law")
    void protectionFromRedPreventsRedSpellTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfLaw());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Disciple of Law and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DiscipleOfLaw()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Law");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
