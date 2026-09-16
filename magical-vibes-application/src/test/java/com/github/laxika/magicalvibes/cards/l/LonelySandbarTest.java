package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LonelySandbar.class, GlorySeeker.class})
class LonelySandbarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new LonelySandbar()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one blue mana")
    void tappingProducesBlueMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LonelySandbar()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lonely Sandbar");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling requires blue mana")
    void cyclingRequiresBlueMana() {
        LonelySandbar sandbar = new LonelySandbar();
        harness.setHand(player1, List.of(sandbar));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sandbar);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addLandReady(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new LonelySandbar());
        land.setSummoningSick(false);
        return land;
    }
}
