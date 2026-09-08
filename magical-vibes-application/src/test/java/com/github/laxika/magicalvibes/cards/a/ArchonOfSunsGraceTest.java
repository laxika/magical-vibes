package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ArchonOfSunsGrace.class, GloriousAnthem.class, GrizzlyBears.class})
class ArchonOfSunsGraceTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control creates a flying Pegasus with lifelink")
    void enchantmentCreatesLifelinkingPegasus() {
        addCreatureReady(player1, new ArchonOfSunsGrace());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent pegasus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PEGASUS))
                .findFirst()
                .orElse(null);
        assertThat(pegasus).isNotNull();
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An enchantment controlled by an opponent does not trigger the Archon")
    void opponentsEnchantmentDoesNotCreatePegasus() {
        addCreatureReady(player1, new ArchonOfSunsGrace());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEGASUS))).isFalse();
    }
}
