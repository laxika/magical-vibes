package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MtendaLion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LionsEyeDiamond.class, MtendaLion.class})
class LionsEyeDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Activating discards the hand, sacrifices itself, and adds three mana of the chosen color")
    void activateDiscardsHandSacrificesAndAddsThreeMana() {
        harness.addToBattlefield(player1, new LionsEyeDiamond());
        harness.setHand(player1, List.of(new MtendaLion(), new MtendaLion()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activating with an empty hand still produces three mana")
    void activateWithEmptyHandStillProducesMana() {
        harness.addToBattlefield(player1, new LionsEyeDiamond());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
