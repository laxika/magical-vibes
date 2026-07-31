package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LakeOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen Swamp and the land enters")
    void entersBySacrificingSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new LakeOfTheDead()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, swamp.getId());

        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Lake of the Dead");
    }

    @Test
    @DisplayName("Declining the sacrifice puts the land into its owner's graveyard")
    void declinedSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new LakeOfTheDead()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Lake of the Dead");
        harness.assertInGraveyard(player1, "Lake of the Dead");
    }

    @Test
    @DisplayName("With no Swamp the land goes straight to the graveyard without a prompt")
    void noSwampSendsLandToGraveyard() {
        harness.setHand(player1, List.of(new LakeOfTheDead()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Lake of the Dead");
        harness.assertInGraveyard(player1, "Lake of the Dead");
    }

    @Test
    @DisplayName("First mana ability adds {B}")
    void manaAbilityAddsBlack() {
        harness.addToBattlefield(player1, new LakeOfTheDead());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Swamp adds {B}{B}{B}{B}")
    void sacrificeAbilityAddsFourBlack() {
        harness.addToBattlefield(player1, new LakeOfTheDead());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Swamp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
    }
}
