package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GhalmaTheShaper.class)
class GhalmaTheShaperTest extends BaseCardTest {

    @Test
    void attackingConjuresTemperedSteelAndCreatesMyrToken() {
        addCreatureReady(player1, new GhalmaTheShaper());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tempered Steel"));

        Permanent myr = findPermanent(player1, "Myr");
        assertThat(myr.getCard().getPower()).isEqualTo(1);
        assertThat(myr.getCard().getToughness()).isEqualTo(1);
        assertThat(myr.getCard().getColor()).isNull();
        assertThat(myr.getCard().getSubtypes()).contains(CardSubtype.MYR);
        assertThat(myr.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(myr.getCard().isToken()).isTrue();
    }
}
