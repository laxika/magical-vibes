package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NevinyrralsDiskTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys all artifacts, creatures, and enchantments but not lands")
    void destroysArtifactsCreaturesAndEnchantments() {
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nevinyrral's Disk");
        harness.assertNotOnBattlefield(player1, "Angel's Feather");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Lands survive.
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Nevinyrral's Disk enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new NevinyrralsDisk()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent disk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nevinyrral's Disk"))
                .findFirst().orElseThrow();
        assertThat(disk.isTapped()).isTrue();
    }
}
