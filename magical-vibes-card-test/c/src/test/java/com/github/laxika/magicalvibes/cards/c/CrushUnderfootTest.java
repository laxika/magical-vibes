package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KithkinDaggerdare;
import com.github.laxika.magicalvibes.cards.l.LowlandOaf;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrushUnderfoot.class, LowlandOaf.class, KithkinDaggerdare.class, CloudcrownOak.class, WoodlandChangeling.class})
class CrushUnderfootTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen Giant deals damage equal to its power to target creature, killing it")
    void giantKillsTargetCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        harness.assertNotOnBattlefield(player2, "Kithkin Daggerdare");
        harness.assertInGraveyard(player2, "Kithkin Daggerdare");
    }

    @Test
    @DisplayName("Target creature survives when Giant's power is less than its toughness")
    void targetSurvivesLesserDamage() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudcrownOak());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        harness.assertOnBattlefield(player2, "Cloudcrown Oak");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot choose a non-Giant creature you control during resolution")
    void cannotChooseNonGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        Permanent nonGiant = harness.addToBattlefieldAndReturn(player1, new KithkinDaggerdare());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(giant.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose an opponent's Giant during resolution")
    void cannotChooseOpponentGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new LowlandOaf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(giant.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastWithoutControllingAGiantAndDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Kithkin Daggerdare");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Crush Underfoot");
    }

    @Test
    void giantEnteringAfterCastingCanBeChosenAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        harness.assertInGraveyard(player2, "Kithkin Daggerdare");
    }

    @Test
    void chosenGiantCanDealDamageToItself() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        harness.assertNotOnBattlefield(player1, "Lowland Oaf");
        harness.assertInGraveyard(player1, "Lowland Oaf");
    }

    @Test
    void changelingCanBeChosenAsTheGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinDaggerdare());
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, giant.getId());

        harness.assertInGraveyard(player2, "Kithkin Daggerdare");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new CrushUnderfoot()));
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
