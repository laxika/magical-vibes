package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SandsteppeOutcastTest extends BaseCardTest {

    @Test
    void putsCounterOnItself() {
        cast(0);

        Permanent outcast = findPermanent(player1, "Sandsteppe Outcast");
        assertThat(outcast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, outcast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outcast)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void createsFlyingSpiritToken() {
        cast(1);

        Permanent outcast = findPermanent(player1, "Sandsteppe Outcast");
        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(outcast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new SandsteppeOutcast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
