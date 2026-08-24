package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkromaAngelOfWrath.class, DarkBanishing.class, Shock.class})
class AkromaAngelOfWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from black and red prevents those spells from targeting it")
    void protectionPreventsBlackAndRedSpellsFromTargeting() {
        Permanent akroma = harness.addToBattlefieldAndReturn(player2, new AkromaAngelOfWrath());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
