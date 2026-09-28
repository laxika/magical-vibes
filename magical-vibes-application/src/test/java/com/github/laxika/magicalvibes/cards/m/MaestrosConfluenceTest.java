package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaestrosConfluence.class, GrizzlyBears.class, Shock.class})
class MaestrosConfluenceTest extends BaseCardTest {

    @Test
    void resolvesAllThreeModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        cast(new int[]{0, 1, 2}, List.of(spell.getId(), creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(-1);
    }

    @Test
    void repeatedMinusThreeModeIsAppliedThreeTimes() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1, 1, 1}, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void goadModeForcesTargetPlayersCreaturesToAttack() {
        addCreatureReady(player2, new GrizzlyBears());
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        cast(new int[]{2, 2, 0}, List.of(player2.getId(), player1.getId(), spell.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void graveyardModeRejectsMulticoloredAndNonSpellCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MaestrosConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, modes,
                null, null, List.of(creature.getId(), creature.getId(), creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new MaestrosConfluence()));
        addMana();
        harness.castSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices), targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
