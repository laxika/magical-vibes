package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralCloak.class, GrizzlyBears.class, Boomerang.class})
class SpectralCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted untapped creature has shroud")
    void enchantedUntappedCreatureHasShroud() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCloak(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Enchanted tapped creature does not have shroud")
    void enchantedTappedCreatureDoesNotHaveShroud() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        attachCloak(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature loses and regains shroud as it is tapped and untapped")
    void shroudFollowsEnchantedCreatureTapState() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCloak(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();

        bears.tap();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();

        bears.untap();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("An enchanted untapped creature cannot be targeted")
    void enchantedUntappedCreatureCannotBeTargeted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCloak(bears);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("A tapped enchanted creature can be targeted")
    void tappedEnchantedCreatureCanBeTargeted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        attachCloak(bears);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    private void attachCloak(Permanent host) {
        Permanent cloak = new Permanent(new SpectralCloak());
        cloak.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(cloak);
    }
}
