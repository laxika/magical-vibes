package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WornPowerstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Worn Powerstone enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new WornPowerstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Worn Powerstone").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Worn Powerstone adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new WornPowerstone());
        GameData gameData = harness.getGameData();
        Permanent powerstone = gameData.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gameData.stack).isEmpty();
    }
}
