package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharedTriumph.class, GrizzlyBears.class, GiantSpider.class})
class SharedTriumphTest extends BaseCardTest {

    @Test
    void creaturesOfChosenTypeOnEitherBattlefieldGetBoosted() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownSpider = addCreatureReady(player1, new GiantSpider());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SharedTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSpider)).isEqualTo(4);
    }
}
