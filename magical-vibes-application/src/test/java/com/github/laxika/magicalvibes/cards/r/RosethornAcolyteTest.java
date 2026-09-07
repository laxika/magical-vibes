package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SeasonalRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RosethornAcolyte.class, SeasonalRitual.class})
class RosethornAcolyteTest extends BaseCardTest {

    @Test
    void adventureAddsManaAndExilesTheCardWithCreatureCastPermission() {
        RosethornAcolyte card = new RosethornAcolyte();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        RosethornAcolyte card = new RosethornAcolyte();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rosethorn Acolyte");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void tapsForOneManaOfAnyColor() {
        harness.addToBattlefield(player1, new RosethornAcolyte());
        Permanent acolyte = findPermanent(player1, "Rosethorn Acolyte");
        acolyte.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(acolyte.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
