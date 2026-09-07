package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteLotusTile.class, GrizzlyBears.class, LlanowarElves.class, WoodElves.class})
class WhiteLotusTileTest extends BaseCardTest {

    @Test
    @DisplayName("White Lotus Tile enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new WhiteLotusTile()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "White Lotus Tile").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping White Lotus Tile adds mana equal to the largest shared creature type")
    void addsManaEqualToLargestSharedCreatureType() {
        Permanent tile = harness.addToBattlefieldAndReturn(player1, new WhiteLotusTile());
        tile.untap();
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new WoodElves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(tile.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
