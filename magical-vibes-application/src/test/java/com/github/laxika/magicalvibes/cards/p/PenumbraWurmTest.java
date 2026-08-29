package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PenumbraWurm.class)
class PenumbraWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Penumbra Wurm dies, it creates a 6/6 black Wurm token with trample")
    void deathCreatesTramplingWurmToken() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PenumbraWurm());
        wurm.setMarkedDamage(6);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Wurm");
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(6);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WURM);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(token.getCard().isToken()).isTrue();
    }
}
