package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DevkarinDissident;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlaiveOfTheGuildpact.class, DevkarinDissident.class, SelesnyaGuildgate.class})
class GlaiveOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Glaive of the Guildpact grants vigilance and menace")
    void equippingGrantsKeywords() {
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(glaive.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each Gate the Equipment controller controls")
    void equippedCreatureGetsGateBonus() {
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        glaive.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new SelesnyaGuildgate());
        harness.addToBattlefield(player1, new SelesnyaGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent-controlled Gates do not increase Glaive of the Guildpact's bonus")
    void opponentGatesDoNotCount() {
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        glaive.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new SelesnyaGuildgate());
        harness.addToBattlefield(player2, new SelesnyaGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void gateBonusUpdatesAsGatesEnterAndLeave() {
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        glaive.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(gate);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void bonusUsesEquipmentControllerEvenWhenCreatureHasDifferentController() {
        Permanent creature = addCreatureReady(player2, new DevkarinDissident());
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        glaive.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new SelesnyaGuildgate());
        harness.addToBattlefield(player2, new SelesnyaGuildgate());
        harness.addToBattlefield(player2, new SelesnyaGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void unattachedEquipmentDoesNotGrantBonuses() {
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        harness.addToBattlefield(player1, new GlaiveOfTheGuildpact());
        harness.addToBattlefield(player1, new SelesnyaGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void reEquippingMovesAllBonusesOnResolution() {
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        Permanent first = addCreatureReady(player1, new DevkarinDissident());
        Permanent second = addCreatureReady(player1, new DevkarinDissident());
        harness.addToBattlefield(player1, new SelesnyaGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(glaive.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isFalse();
        harness.passBothPriorities();

        assertThat(glaive.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }

    @Test
    void equipCostsThreeGenericMana() {
        Permanent glaive = harness.addToBattlefieldAndReturn(player1, new GlaiveOfTheGuildpact());
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(glaive.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new GlaiveOfTheGuildpact());
        Permanent creature = addCreatureReady(player2, new DevkarinDissident());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new GlaiveOfTheGuildpact());
        Permanent creature = addCreatureReady(player1, new DevkarinDissident());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
