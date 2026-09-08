package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KhalniGemTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns two lands you control")
    void etbReturnsTwoLands() {
        UUID firstLandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID secondLandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID thirdLandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.addToBattlefield(player2, new Island());

        harness.setHand(player1, List.of(new KhalniGem()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstLandId, secondLandId, thirdLandId);

        harness.handlePermanentChosen(player1, firstLandId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(secondLandId, thirdLandId);

        harness.handlePermanentChosen(player1, secondLandId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(thirdLandId, harness.getPermanentId(player1, "Khalni Gem"));
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Tapping Khalni Gem adds two mana of the chosen color")
    void tappingAddsTwoManaOfChosenColor() {
        harness.addToBattlefield(player1, new KhalniGem());
        Permanent gem = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gem.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
