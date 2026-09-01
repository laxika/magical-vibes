package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineEvangelist.class, GrizzlyBears.class, Shock.class})
class SanguineEvangelistTest extends BaseCardTest {

    @Test
    @DisplayName("When Sanguine Evangelist enters, it creates a 1/1 black Bat token with flying")
    void entersCreatesBatToken() {
        castEvangelist();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.getCard().isToken()).isTrue();
        assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(bat.getCard().getSubtypes()).contains(CardSubtype.BAT);
        assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(bat.getEffectivePower()).isEqualTo(1);
        assertThat(bat.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Sanguine Evangelist dies, it creates a Bat token")
    void diesCreatesBatToken() {
        Permanent evangelist = addCreatureReady(player1, new SanguineEvangelist());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, evangelist.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("Battle Cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent evangelist = addCreatureReady(player1, new SanguineEvangelist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(evangelist.getPowerModifier()).isZero();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
    }

    private void castEvangelist() {
        harness.setHand(player1, List.of(new SanguineEvangelist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
