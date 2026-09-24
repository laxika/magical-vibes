package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunawayTrashBot.class, AngelsFeather.class, GloriousAnthem.class, GrizzlyBears.class})
class RunawayTrashBotTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact or enchantment card in its controller's graveyard")
    void boostsForArtifactOrEnchantmentCards() {
        Permanent bot = addBot(player1);
        harness.setGraveyard(player1, List.of(
                new AngelsFeather(), new GloriousAnthem(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new AngelsFeather(), new GloriousAnthem()));

        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bot)).isEqualTo(4);
    }

    @Test
    @DisplayName("Updates dynamically as qualifying cards enter or leave its controller's graveyard")
    void updatesDynamically() {
        Permanent bot = addBot(player1);
        harness.setGraveyard(player1, List.of(new AngelsFeather()));

        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bot)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new GloriousAnthem(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bot)).isEqualTo(4);
    }

    private Permanent addBot(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new RunawayTrashBot());
    }
}
