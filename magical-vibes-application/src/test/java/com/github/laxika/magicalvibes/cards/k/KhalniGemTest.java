package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.s.ScuteMob;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KhalniGem.class, Island.class, AshayaSoulOfTheWild.class, ScuteMob.class})
class KhalniGemTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns two lands you control")
    void etbReturnsTwoLands() {
        UUID firstLandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID secondLandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID thirdLandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.addToBattlefield(player2, new Island());

        harness.castFromHand(player1, new KhalniGem(), "{4}");
        resolveAllTriggers();

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

    @Test
    @DisplayName("ETB with one land returns that land and finishes resolving")
    void etbWithOneLand() {
        UUID landId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.castFromHand(player1, new KhalniGem(), "{4}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, landId);

        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Khalni Gem");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB with no lands leaves Khalni Gem on the battlefield")
    void etbWithNoLands() {
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new KhalniGem(), "{4}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Khalni Gem");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both lands remain eligible until the simultaneous return, including Ashaya's creature lands")
    void returnsAshayaAndAnotherCreatureLandSimultaneously() {
        UUID ashayaId = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild()).getId();
        UUID scuteMobId = harness.addToBattlefieldAndReturn(player1, new ScuteMob()).getId();
        harness.castFromHand(player1, new KhalniGem(), "{4}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(ashayaId, scuteMobId);
        harness.handlePermanentChosen(player1, ashayaId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(scuteMobId);
        harness.handlePermanentChosen(player1, scuteMobId);

        harness.assertInHand(player1, "Ashaya, Soul of the Wild");
        harness.assertInHand(player1, "Scute Mob");
        harness.assertOnBattlefield(player1, "Khalni Gem");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land controlled by you returns to its owner's hand")
    void returnsLandToOwnerRatherThanController() {
        Island borrowedLand = new Island();
        borrowedLand.setOwnerId(player2.getId());
        UUID landId = harness.addToBattlefieldAndReturn(player1, borrowedLand).getId();
        harness.castFromHand(player1, new KhalniGem(), "{4}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, landId);

        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Khalni Gem");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
