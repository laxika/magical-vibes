package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandersPlate.class, GrizzlyBears.class})
class CommandersPlateTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent plate = addPlateReady(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Protection excludes colors in the commander's color identity")
    void protectionUsesCommanderColorIdentity() {
        gd.playerCommandZones.get(player1.getId()).add(new GrizzlyBears());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent plate = addPlateReady(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isFalse();

        gd.playerCommandZones.get(player1.getId()).clear();

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Equip commander only targets a commander")
    void restrictedEquipTargetsCommander() {
        Permanent plate = addPlateReady(player1);
        Permanent nonCommander = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonCommander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("commander");
        assertThat(plate.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip commander attaches to a commander")
    void restrictedEquipAttachesToCommander() {
        Permanent plate = addPlateReady(player1);
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        commander.setCommander(true);
        gd.playerCommanders.get(player1.getId()).add(commander.getCard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, commander.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(commander.getId());
    }

    @Test
    @DisplayName("The generic equip ability attaches to any creature you control")
    void genericEquipAttachesToNonCommander() {
        Permanent plate = addPlateReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addPlateReady(Player player) {
        Permanent permanent = new Permanent(new CommandersPlate());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

}
