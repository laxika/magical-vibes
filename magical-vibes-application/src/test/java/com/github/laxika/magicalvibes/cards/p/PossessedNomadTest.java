package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessedNomad.class, Spellbook.class, SavannahLions.class, GrizzlyBears.class})
class PossessedNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and becomes black at threshold")
    void thresholdBonus() {
        fillGraveyard(player1, 7);
        Permanent nomad = harness.addToBattlefieldAndReturn(player1, new PossessedNomad());

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, nomad)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Does not get threshold abilities below seven cards")
    void noThresholdBonus() {
        fillGraveyard(player1, 6);
        Permanent nomad = addReadyNomad();

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, nomad, CardColor.BLACK)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a target white creature at threshold")
    void destroysWhiteCreature() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Savannah Lions");
        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("Cannot target a nonwhite creature")
    void cannotTargetNonwhiteCreature() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyNomad() {
        Permanent nomad = harness.addToBattlefieldAndReturn(player1, new PossessedNomad());
        nomad.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nomad;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
