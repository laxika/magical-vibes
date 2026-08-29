package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GalazethPrismariTest extends BaseCardTest {

    @Test
    void entersAndCreatesTreasure() {
        castGalazeth();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void artifactsGainRestrictedAnyColorManaAbility() {
        castGalazeth();
        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        harness.activateAbility(player1, treasureIndex, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isEqualTo(1);
    }

    private void castGalazeth() {
        harness.setHand(player1, List.of(new GalazethPrismari()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
