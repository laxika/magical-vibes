package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PhyrexianBroodlings;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraftedButcher.class, PhyrexianBroodlings.class, GrizzlyBears.class, Spellbook.class})
class GraftedButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability grants menace to your Phyrexians")
    void grantsMenaceToYourPhyrexians() {
        Permanent ownPhyrexian = harness.addToBattlefieldAndReturn(player1, new PhyrexianBroodlings());
        Permanent ownNonPhyrexian = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPhyrexian = harness.addToBattlefieldAndReturn(player2, new PhyrexianBroodlings());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GraftedButcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent butcher = findPermanent(player1, "Grafted Butcher");
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownPhyrexian, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonPhyrexian, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentPhyrexian, Keyword.MENACE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownPhyrexian, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Other Phyrexians you control get +1/+1")
    void boostsOtherPhyrexiansYouControl() {
        Permanent butcher = harness.addToBattlefieldAndReturn(player1, new GraftedButcher());
        Permanent ownPhyrexian = harness.addToBattlefieldAndReturn(player1, new PhyrexianBroodlings());
        Permanent ownNonPhyrexian = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPhyrexian = harness.addToBattlefieldAndReturn(player2, new PhyrexianBroodlings());

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, butcher)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownPhyrexian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownPhyrexian)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonPhyrexian)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentPhyrexian)).isEqualTo(2);
    }

    @Test
    @DisplayName("The graveyard ability sacrifices a creature and returns Grafted Butcher")
    void returnsFromGraveyardBySacrificingCreature() {
        GraftedButcher butcher = new GraftedButcher();
        harness.setGraveyard(player1, List.of(butcher));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addReturnMana();
        setSorcerySpeed();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grafted Butcher");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard ability can sacrifice an artifact")
    void returnsFromGraveyardBySacrificingArtifact() {
        GraftedButcher butcher = new GraftedButcher();
        harness.setGraveyard(player1, List.of(butcher));
        harness.addToBattlefield(player1, new Spellbook());
        addReturnMana();
        setSorcerySpeed();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grafted Butcher");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("The graveyard ability is sorcery speed")
    void graveyardAbilityIsSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new GraftedButcher()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addReturnMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReturnMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void setSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
