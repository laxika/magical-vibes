package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuickDrawKatana.class, BearCub.class})
class QuickDrawKatanaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuickDrawKatana());
        katana.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has first strike during its controller's turn")
    void equippedCreatureHasFirstStrikeDuringControllerTurn() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuickDrawKatana());
        katana.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does not have first strike during its controller's opponent's turn")
    void equippedCreatureDoesNotHaveFirstStrikeDuringOpponentTurn() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuickDrawKatana());
        katana.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip moves the katana and its effects to another creature")
    void equipMovesKatanaAndEffects() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuickDrawKatana());
        Permanent firstCreature = addCreatureReady(player1, new BearCub());
        Permanent secondCreature = addCreatureReady(player1, new BearCub());
        katana.setAttachedTo(firstCreature.getId());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(katana.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses follow the Equipment controller's turn when creature control differs")
    void bonusesFollowEquipmentControllerTurn() {
        Permanent creature = addCreatureReady(player2, new BearCub());
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuickDrawKatana());
        katana.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Unattached katana grants no bonuses")
    void unattachedKatanaGrantsNoBonuses() {
        Permanent creature = addCreatureReady(player1, new BearCub());
        harness.addToBattlefield(player1, new QuickDrawKatana());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new QuickDrawKatana());
        Permanent creature = addCreatureReady(player2, new BearCub());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(katana.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new QuickDrawKatana());
        Permanent creature = addCreatureReady(player1, new BearCub());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
