package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({StormcatchMentor.class, AngelsMercy.class, Divination.class, GrizzlyBears.class, Shock.class})
class StormcatchMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces an instant's cost by {1} for its controller")
    void reducesInstantCost() {
        harness.addToBattlefield(player1, new StormcatchMentor());
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Reduces a sorcery's cost by {1} for its controller")
    void reducesSorceryCost() {
        harness.addToBattlefield(player1, new StormcatchMentor());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Does not reduce creature spells or the opponent's spells")
    void scopeIsInstantSorceryAndControllerOnly() {
        harness.addToBattlefield(player1, new StormcatchMentor());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new AngelsMercy()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prowess boosts it when its controller casts a noncreature spell")
    void prowessBoostsAfterNoncreatureSpell() {
        harness.addToBattlefield(player1, new StormcatchMentor());
        Permanent mentor = gd.playerBattlefields.get(player1.getId()).getFirst();
        int powerBeforeCast = gqs.getEffectivePower(gd, mentor);
        int toughnessBeforeCast = gqs.getEffectiveToughness(gd, mentor);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(powerBeforeCast + 1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(toughnessBeforeCast + 1);
    }

    @Test
    @DisplayName("Haste allows it to attack immediately after entering")
    void hasteAllowsImmediateAttack() {
        harness.setHand(player1, List.of(new StormcatchMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
