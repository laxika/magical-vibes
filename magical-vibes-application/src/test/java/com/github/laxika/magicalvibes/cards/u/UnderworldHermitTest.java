package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UnderworldHermit.class)
class UnderworldHermitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates Squirrels equal to your black devotion, including this creature")
    void etbCreatesSquirrelsEqualToBlackDevotion() {
        harness.setHand(player1, List.of(new UnderworldHermit()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squirrels = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .toList();

        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getPower()).isEqualTo(1);
            assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
        });
    }
}
