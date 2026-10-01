package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nightcreep.class, AssaultZeppelid.class, NovijenHeartOfProgress.class})
class NightcreepTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all creatures black and all lands Swamps")
    void affectsCreaturesAndLandsControlledByBothPlayers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new NovijenHeartOfProgress());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());

        castNightcreep();

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, opponentCreature)).containsExactly(CardColor.BLACK);
        assertThat(gqs.computeStaticBonus(gd, ownLand).grantedSubtypes())
                .containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.computeStaticBonus(gd, opponentLand).grantedSubtypes())
                .containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("A nonbasic land changed to a Swamp produces only black mana")
    void changedLandProducesBlackMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NovijenHeartOfProgress());

        castNightcreep();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(land));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Only permanents on the battlefield when Nightcreep resolves are affected")
    void laterPermanentsAreNotAffected() {
        castNightcreep();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent laterLand = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());

        assertThat(gqs.hasColor(gd, laterCreature, CardColor.BLACK)).isFalse();
        StaticBonus laterLandBonus = gqs.computeStaticBonus(gd, laterLand);
        assertThat(laterLandBonus.grantedSubtypes()).doesNotContain(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("The changes wear off at end of turn")
    void changesWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NovijenHeartOfProgress());

        castNightcreep();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasColor(gd, creature, CardColor.BLACK)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, land).grantedSubtypes())
                .doesNotContain(CardSubtype.SWAMP);
    }

    private void castNightcreep() {
        harness.setHand(player1, List.of(new Nightcreep()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
