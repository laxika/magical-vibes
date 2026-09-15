package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndemicPlague.class, ElvishWarrior.class, WirewoodElf.class, GoblinSkyRaider.class,
        GlorySeeker.class})
class EndemicPlagueTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesSharingAnySacrificedCreatureType() {
        Permanent sacrificed = addCreatureReady(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new WirewoodElf());
        harness.addToBattlefield(player2, new GoblinSkyRaider());
        harness.addToBattlefield(player2, new GlorySeeker());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player1, "Wirewood Elf");
        harness.assertNotOnBattlefield(player2, "Goblin Sky Raider");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    void destructionDoesNotAllowRegeneration() {
        Permanent sacrificed = addCreatureReady(player1, new ElvishWarrior());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new WirewoodElf());
        matchingCreature.setRegenerationShield(1);
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wirewood Elf");
        harness.assertInGraveyard(player2, "Wirewood Elf");
    }

    @Test
    void cannotCastWithoutACreatureToSacrifice() {
        harness.setHand(player1, List.of(new EndemicPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new EndemicPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
