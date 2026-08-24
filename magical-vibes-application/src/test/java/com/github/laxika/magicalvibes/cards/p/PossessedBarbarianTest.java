package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PardicLancer;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessedBarbarian.class, PardicLancer.class, GrizzlyBears.class, Spellbook.class})
class PossessedBarbarianTest extends BaseCardTest {

    @Test
    void thresholdGivesBonusColorAndAbility() {
        fillGraveyard(player1, 7);
        Permanent barbarian = addReadyBarbarian();

        assertThat(gqs.getEffectivePower(gd, barbarian)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barbarian)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, barbarian)).containsExactly(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, barbarian)).hasSize(1);
    }

    @Test
    void abilityDestroysTargetRedCreature() {
        fillGraveyard(player1, 7);
        addReadyBarbarian();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PardicLancer());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pardic Lancer");
        harness.assertInGraveyard(player2, "Pardic Lancer");
    }

    @Test
    void abilityCannotTargetNonredCreature() {
        fillGraveyard(player1, 7);
        addReadyBarbarian();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red creature");
    }

    @Test
    void thresholdBonusesAndAbilityDisappearBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent barbarian = addReadyBarbarian();
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, barbarian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, barbarian)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, barbarian)).doesNotContain(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, barbarian)).isEmpty();
    }

    private Permanent addReadyBarbarian() {
        Permanent barbarian = harness.addToBattlefieldAndReturn(player1, new PossessedBarbarian());
        barbarian.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return barbarian;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
