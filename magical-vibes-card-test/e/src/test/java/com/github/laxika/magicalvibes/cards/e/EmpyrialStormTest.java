package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmpyrialStorm.class, GrizzlyBears.class})
class EmpyrialStormTest extends BaseCardTest {

    @Test
    void createsOneAngelToken() {
        cast();

        assertThat(angelTokens()).hasSize(1);
        assertThat(angelTokens()).allSatisfy(angel -> {
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
            assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        });
    }

    @Test
    void copiesItselfForEachCommanderCastFromCommandZone() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 4);

        cast();

        assertThat(angelTokens()).hasSize(3);
    }

    private void cast() {
        harness.castFromHand(player1, new EmpyrialStorm(), "{4}{W}{W}");
        resolveAllTriggers();
    }

    private List<Permanent> angelTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Angel"))
                .toList();
    }
}
