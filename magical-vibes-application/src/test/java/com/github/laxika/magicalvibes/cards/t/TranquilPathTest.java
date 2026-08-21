package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilPath.class, AngelicChorus.class, RuleOfLaw.class, GrizzlyBears.class, Forest.class})
class TranquilPathTest extends BaseCardTest {

    @Test
    void destroysAllEnchantmentsAndDrawsACard() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TranquilPath()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
