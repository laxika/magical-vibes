package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Meekstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NevinyrralsDisk.class, Meekstone.class, BadMoon.class, GrizzlyBears.class, Forest.class})
class NevinyrralsDiskTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys all artifacts, creatures, and enchantments but not lands")
    void destroysArtifactsCreaturesAndEnchantments() {
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        harness.addToBattlefield(player1, new Meekstone());
        harness.addToBattlefield(player1, new BadMoon());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nevinyrral's Disk");
        harness.assertNotOnBattlefield(player1, "Meekstone");
        harness.assertNotOnBattlefield(player1, "Bad Moon");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Lands survive.
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Nevinyrral's Disk enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new NevinyrralsDisk(), "{4}");
        harness.passBothPriorities();

        Permanent disk = findPermanent(player1, "Nevinyrral's Disk");
        assertThat(disk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The destruction ability cannot be activated while the Disk is tapped")
    void cannotActivateWhileTapped() {
        harness.castFromHand(player1, new NevinyrralsDisk(), "{4}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Nevinyrral's Disk");
    }
}
