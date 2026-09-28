package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Friarball.class, Forest.class, GrizzlyBears.class, Shock.class})
class FriarballTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 2/2 white Monk token when no other cards were played")
    void createsOneMonkToken() {
        castFriarball();

        assertThat(monkTokens()).hasSize(1);
        assertThat(monkTokens().getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.MONK);
        assertThat(monkTokens().getFirst().getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.WHITE);
    }

    @Test
    @DisplayName("Coststorm copies for each distinct prior spell and land mana value")
    void coststormCopiesForDistinctManaValues() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new Shock());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        castFriarball();

        assertThat(monkTokens()).hasSize(4);
    }

    private void castFriarball() {
        harness.castFromHand(player1, new Friarball(), "{3}{W}");
        resolveAllTriggers();
    }

    private List<Permanent> monkTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Monk"))
                .toList();
    }
}
