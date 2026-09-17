package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelRevival;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymbioticWurm.class, CruelRevival.class})
class SymbioticWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Symbiotic Wurm dies, its controller creates seven Insect tokens")
    void deathTriggerCreatesSevenInsectTokens() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SymbioticWurm());

        harness.setHand(player2, List.of(new CruelRevival()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, List.of(wurm.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Symbiotic Wurm");
        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(7);
        assertThat(insects).allSatisfy(insect -> {
            assertThat(insect.getCard().isToken()).isTrue();
            assertThat(insect.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(insect.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(insect.getCard().getPower()).isEqualTo(1);
            assertThat(insect.getCard().getToughness()).isEqualTo(1);
            assertThat(insect.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        });
        harness.assertNotOnBattlefield(player2, "Insect");
    }
}
