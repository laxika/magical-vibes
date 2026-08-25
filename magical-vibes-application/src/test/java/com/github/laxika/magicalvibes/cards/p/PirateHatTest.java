package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DaringSaboteur;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PirateHat.class, DaringSaboteur.class, Forest.class, GrizzlyBears.class})
class PirateHatTest extends BaseCardTest {

    @Test
    @DisplayName("Equip Pirate attaches Pirate Hat and boosts the Pirate")
    void pirateEquipAttachesAndBoostsPirate() {
        Permanent hat = addHatReady(player1);
        Permanent pirate = addCreatureReady(player1, new DaringSaboteur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, pirate.getId());
        harness.passBothPriorities();

        assertThat(hat.getAttachedTo()).isEqualTo(pirate.getId());
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(2);
    }

    @Test
    @DisplayName("Generic equip attaches Pirate Hat to a non-Pirate creature")
    void genericEquipAttachesToNonPirate() {
        Permanent hat = addHatReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hat.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip Pirate rejects a non-Pirate creature")
    void pirateEquipRejectsNonPirate() {
        addHatReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pirate creature");
    }

    @Test
    @DisplayName("Attacking with the equipped creature draws then discards")
    void attackTriggersLoot() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent hat = addHatReady(player1);
        Permanent pirate = addCreatureReady(player1, new DaringSaboteur());
        hat.setAttachedTo(pirate.getId());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    private Permanent addHatReady(Player player) {
        Permanent hat = new Permanent(new PirateHat());
        hat.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(hat);
        return hat;
    }
}
