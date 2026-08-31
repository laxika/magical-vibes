package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LakeOfTheDead.class, Swamp.class})
class LakeOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen Swamp and the land enters")
    void entersBySacrificingSwamp() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new LakeOfTheDead()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, swamp.getId());

        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Lake of the Dead");
    }

    @Test
    void opponentSwampCannotBeSacrificed() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        LakeOfTheDead lakeCard = new LakeOfTheDead();
        harness.setHand(player1, List.of(lakeCard));
        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(swamp);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lakeCard);
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
    void nonSwampCannotBeSacrificed() {
        Permanent existingLake = harness.addToBattlefieldAndReturn(player1, new LakeOfTheDead());
        LakeOfTheDead entering = new LakeOfTheDead();
        harness.setHand(player1, List.of(entering));
        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(existingLake);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card == entering);
    }

    @Test
    void directEntryUsesReplacement() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        LakeOfTheDead lakeCard = new LakeOfTheDead();
        Permanent lake = harness.enterBattlefieldAndReturn(player1, lakeCard);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, swamp.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lake);
    }

    @Test
    @DisplayName("First mana ability adds {B}")
    void manaAbilityAddsBlack() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new LakeOfTheDead());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lake.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering can sacrifice a tapped Swamp")
    void entersBySacrificingTappedSwamp() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        LakeOfTheDead lakeCard = new LakeOfTheDead();
        harness.setHand(player1, List.of(lakeCard));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, swamp.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == lakeCard);
    }

    @Test
    @DisplayName("Sacrificing a Swamp adds {B}{B}{B}{B}")
    void sacrificeAbilityAddsFourBlack() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new LakeOfTheDead());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Swamp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
    }

    @Test
    void sacrificeAbilityRequiresSwamp() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new LakeOfTheDead());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lake.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void lakeCanPayItsOwnSwampCost() {
        LakeOfTheDead lakeCard = new LakeOfTheDead();
        lakeCard.addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.SWAMP, GrantScope.SELF));
        Permanent lake = harness.addToBattlefieldAndReturn(player1, lakeCard);

        assertThat(gqs.effectiveBasicLandTypes(gd, lake)).contains(CardSubtype.SWAMP);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(lake.isTapped()).isTrue();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lakeCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
    }
}
