package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatteryBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Battery Bearer gives your creatures a Powerstone mana ability")
    void grantsRestrictedManaAbilityToYourCreatures() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        bears.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an artifact with mana value 6 or greater triggers a draw")
    void largeArtifactSpellTriggersDraw() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting an artifact with mana value less than 6 does not trigger a draw")
    void smallArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BatteryBearer());
        harness.setHand(player1, List.of(new GildedLotus()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
