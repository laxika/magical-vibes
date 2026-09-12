package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({VeiledSerpent.class, WornPowerstone.class, Island.class, Annul.class})
class VeiledSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Serpent creature when an opponent casts a spell")
    void becomesSerpentCreatureWhenOpponentCastsSpell() {
        Permanent serpent = transformSerpent();

        assertThat(gqs.isCreature(gd, serpent)).isTrue();
        assertThat(gqs.isEnchantment(gd, serpent)).isFalse();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, serpent)).containsExactly(CardSubtype.SERPENT);
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a spell")
    void doesNotTriggerForControllerCast() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new VeiledSerpent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new WornPowerstone(), "{3}");

        assertThat(gqs.isEnchantment(gd, serpent)).isTrue();
        assertThat(gqs.isCreature(gd, serpent)).isFalse();
    }

    @Test
    @DisplayName("Transforms when an opponent's spell is countered")
    void transformsWhenOpponentSpellIsCountered() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new VeiledSerpent());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        WornPowerstone powerstone = new WornPowerstone();
        harness.setHand(player2, List.of(powerstone));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new Annul()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, powerstone.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, serpent)).isTrue();
        harness.assertInGraveyard(player2, "Worn Powerstone");
    }

    @Test
    @DisplayName("Cannot attack unless the defending player controls an Island")
    void cannotAttackWithoutDefendingIsland() {
        Permanent serpent = transformSerpent();
        serpent.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when the defending player controls an Island")
    void canAttackWithDefendingIsland() {
        Permanent serpent = transformSerpent();
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new Island());

        declareAttackers(List.of(0));

        assertThat(serpent.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards Veiled Serpent and draws a card")
    void cyclingDiscardsAndDraws() {
        harness.setHand(player1, List.of(new VeiledSerpent()));
        harness.setLibrary(player1, List.of(new WornPowerstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Veiled Serpent");
        harness.assertInHand(player1, "Worn Powerstone");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent transformSerpent() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new VeiledSerpent());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WornPowerstone(), "{3}");
        harness.passBothPriorities();
        return serpent;
    }
}
