package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OasisGardener.class)
class OasisGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains its controller 2 life")
    void entersAndGainsLife() {
        harness.setHand(player1, List.of(new OasisGardener()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
    }

    @Test
    @DisplayName("Tapping Oasis Gardener prompts for a color and adds one mana")
    void tapsForAnyColorMana() {
        Permanent gardener = new Permanent(new OasisGardener());
        gardener.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gardener);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gardener.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
