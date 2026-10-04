package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GildedPinions.class, CivicGardener.class})
class GildedPinionsTest extends BaseCardTest {

    @Test
    @DisplayName("When Gilded Pinions enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.castFromHand(player1, new GildedPinions(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Equipped creature gets flying")
    void equippedCreatureGetsFlying() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        addPinionsReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Re-equipping moves flying from the old creature to the new creature")
    void reequippingMovesFlying() {
        Permanent pinions = addPinionsReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Equip requires two mana")
    void cannotEquipWithOnlyOneMana() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pinions.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        addPinionsReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Treasure trigger survives the Equipment leaving the battlefield")
    void treasureTriggerSurvivesEquipmentLeaving() {
        harness.castFromHand(player1, new GildedPinions(), "{2}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        Permanent pinions = findPermanents(player1, "Gilded Pinions").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pinions));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gilded Pinions")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private Permanent addPinionsReady(Player player) {
        Permanent pinions = harness.addToBattlefieldAndReturn(player, new GildedPinions());
        pinions.setSummoningSick(false);
        return pinions;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new CivicGardener());
        creature.setSummoningSick(false);
        return creature;
    }
}
