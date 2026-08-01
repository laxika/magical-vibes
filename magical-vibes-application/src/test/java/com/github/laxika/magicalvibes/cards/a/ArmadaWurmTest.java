package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmadaWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 5/5 green Wurm token with trample")
    void etbCreatesWurmToken() {
        harness.setHand(player1, List.of(new ArmadaWurm()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve the creature spell
        harness.passBothPriorities(); // Resolve the ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Wurm");
        assertThat(tokens).hasSize(1);

        Permanent wurm = tokens.getFirst();
        assertThat(wurm.getCard().getPower()).isEqualTo(5);
        assertThat(wurm.getCard().getToughness()).isEqualTo(5);
        assertThat(wurm.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wurm.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wurm.getCard().getSubtypes()).contains(CardSubtype.WURM);
        assertThat(wurm.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(wurm.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The token is created under the controller's control")
    void tokenGoesToController() {
        harness.setHand(player2, List.of(new ArmadaWurm()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Wurm")).hasSize(1);
        assertThat(findPermanents(player1, "Wurm")).isEmpty();
    }
}
