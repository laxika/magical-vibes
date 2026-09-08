package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticSkull.class, Forest.class})
class MysticSkullTest extends BaseCardTest {

    @Test
    void firstAbilityAddsAnyColorMana() {
        Permanent skull = addReadySkull(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, skull), 0, null, null);

        assertThat(skull.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void secondAbilityTransformsTheArtifact() {
        Permanent skull = addReadySkull(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, skull), 1, null, null);
        harness.passBothPriorities();

        assertThat(skull.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceGivesControlledLandsAnAnyColorAbility() {
        Permanent skull = addTransformedSkull(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, indexOf(player1, forest), 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(skull.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceDoesNotGiveOpponentsLandsAnAbility() {
        addTransformedSkull(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, forest), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addReadySkull(Player player) {
        MysticSkull card = new MysticSkull();
        Permanent skull = new Permanent(card);
        skull.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(skull);
        return skull;
    }

    private Permanent addTransformedSkull(Player player) {
        MysticSkull card = new MysticSkull();
        Permanent skull = new Permanent(card);
        skull.setSummoningSick(false);
        skull.setCard(card.getBackFaceCard());
        skull.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(skull);
        return skull;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
