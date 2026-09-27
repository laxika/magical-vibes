package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.o.ONaginata;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManrikiGusari.class, ArabaMothrider.class, ONaginata.class, PithingNeedle.class})
class ManrikiGusariTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new ArabaMothrider());
        Permanent otherCreature = addCreatureReady(player1, new ArabaMothrider());
        Permanent manrikiGusari = harness.addToBattlefieldAndReturn(player1, new ManrikiGusari());
        manrikiGusari.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {1} attaches Manriki-Gusari and grants its bonus")
    void equipAttachesAndBoostsCreature() {
        Permanent manrikiGusari = harness.addToBattlefieldAndReturn(player1, new ManrikiGusari());
        Permanent creature = addCreatureReady(player1, new ArabaMothrider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(manrikiGusari.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature can tap to destroy target Equipment")
    void equippedCreatureDestroysTargetEquipment() {
        Permanent creature = addCreatureReady(player1, new ArabaMothrider());
        Permanent manrikiGusari = harness.addToBattlefieldAndReturn(player1, new ManrikiGusari());
        manrikiGusari.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ONaginata());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "O-Naginata");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature cannot target a non-Equipment artifact")
    void cannotTargetNonEquipmentArtifact() {
        Permanent creature = addCreatureReady(player1, new ArabaMothrider());
        Permanent manrikiGusari = harness.addToBattlefieldAndReturn(player1, new ManrikiGusari());
        manrikiGusari.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
