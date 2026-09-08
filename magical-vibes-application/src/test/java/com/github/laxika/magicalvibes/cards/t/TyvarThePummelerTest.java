package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyvarThePummeler.class, GrizzlyBears.class, HillGiant.class})
class TyvarThePummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another creature grants indestructible and taps Tyvar")
    void tappingAnotherCreatureGrantsIndestructibleAndTapsTyvar() {
        Permanent tyvar = addCreatureReady(player1, new TyvarThePummeler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar), null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(bears.getId(), hillGiant.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(tyvar.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, tyvar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The indestructible ability can be activated while Tyvar is tapped")
    void canActivateIndestructibleAbilityWhileTapped() {
        Permanent tyvar = addCreatureReady(player1, new TyvarThePummeler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        tyvar.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar), null, null);
        harness.passBothPriorities();

        assertThat(tyvar.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, tyvar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The pump uses the greatest controlled creature power")
    void pumpUsesGreatestControlledCreaturePower() {
        Permanent tyvar = addCreatureReady(player1, new TyvarThePummeler());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar), 1, null, null);
        harness.passBothPriorities();

        assertThat(tyvar.getEffectivePower()).isEqualTo(6);
        assertThat(tyvar.getEffectiveToughness()).isEqualTo(6);
        assertThat(hillGiant.getEffectivePower()).isEqualTo(6);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(6);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent tyvar = addCreatureReady(player1, new TyvarThePummeler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tyvar), 1, null, null);
        harness.passBothPriorities();
        assertThat(tyvar.getEffectivePower()).isEqualTo(6);
        assertThat(tyvar.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tyvar.getEffectivePower()).isEqualTo(3);
        assertThat(tyvar.getEffectiveToughness()).isEqualTo(3);
    }
}
