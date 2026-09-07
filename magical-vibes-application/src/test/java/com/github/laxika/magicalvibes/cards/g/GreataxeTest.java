package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Greataxe.class, GrizzlyBears.class})
class GreataxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Greataxe gives the creature +4/+0")
    void equippingGivesPowerBoost() {
        Permanent greataxe = addReadyGreataxe(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(greataxe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unequipped creature does not get Greataxe's boost")
    void unequippedCreatureIsNotBoosted() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReadyGreataxe(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Greataxe cannot equip an opponent's creature")
    void cannotEquipOpponentCreature() {
        addReadyGreataxe(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGreataxe(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Greataxe());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
