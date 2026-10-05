package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.cards.h.HissingMiasma;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mortify.class, GhorClanSavage.class, HissingMiasma.class, IzzetSignet.class})
class MortifyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new GhorClanSavage());
        castMortifyAt(harness.getPermanentId(player2, "Ghor-Clan Savage"));

        harness.assertInGraveyard(player2, "Ghor-Clan Savage");
    }

    @Test
    @DisplayName("Destroys a targeted enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new HissingMiasma());
        castMortifyAt(harness.getPermanentId(player2, "Hissing Miasma"));

        harness.assertInGraveyard(player2, "Hissing Miasma");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonenchantment permanent")
    void cannotTargetIzzetSignet() {
        harness.addToBattlefield(player2, new IzzetSignet());
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana();

        UUID targetId = harness.getPermanentId(player2, "Izzet Signet");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    @Test
    @DisplayName("Can destroy its controller's creature")
    void destroysOwnCreature() {
        harness.addToBattlefield(player1, new GhorClanSavage());
        castMortifyAt(harness.getPermanentId(player1, "Ghor-Clan Savage"));

        harness.assertNotOnBattlefield(player1, "Ghor-Clan Savage");
        harness.assertInGraveyard(player1, "Ghor-Clan Savage");
        harness.assertInGraveyard(player1, "Mortify");
    }

    @Test
    @DisplayName("Can destroy its controller's enchantment")
    void destroysOwnEnchantment() {
        harness.addToBattlefield(player1, new HissingMiasma());
        castMortifyAt(harness.getPermanentId(player1, "Hissing Miasma"));

        harness.assertNotOnBattlefield(player1, "Hissing Miasma");
        harness.assertInGraveyard(player1, "Hissing Miasma");
    }

    @Test
    @DisplayName("Does not resolve when its only target leaves the battlefield")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GhorClanSavage());
        harness.addToBattlefield(player2, new HissingMiasma());
        UUID targetId = harness.getPermanentId(player2, "Ghor-Clan Savage");
        harness.setHand(player1, List.of(new Mortify(), new Mortify()));
        addMortifyMana();
        addMortifyMana();
        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Ghor-Clan Savage");
        harness.assertInGraveyard(player2, "Ghor-Clan Savage");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Mortify)
                .hasSize(2);
        harness.assertOnBattlefield(player2, "Hissing Miasma");
    }

    private void castMortifyAt(UUID targetId) {
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMortifyMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
