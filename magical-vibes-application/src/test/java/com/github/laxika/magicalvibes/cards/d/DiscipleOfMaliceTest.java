package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscipleOfMalice.class, Pacifism.class, GrizzlyBears.class})
class DiscipleOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from white prevents white spells from targeting Disciple of Malice")
    void protectionFromWhitePreventsWhiteSpellTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfMalice());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Disciple of Malice and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DiscipleOfMalice()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Malice");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
