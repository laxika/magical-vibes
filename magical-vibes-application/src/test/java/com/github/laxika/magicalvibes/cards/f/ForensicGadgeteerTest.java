package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForensicGadgeteer.class, MagnifyingGlass.class, GrizzlyBears.class})
class ForensicGadgeteerTest extends BaseCardTest {

    @Test
    void investigatesWhenControllerCastsArtifactSpell() {
        addCreatureReady(player1, new ForensicGadgeteer());
        harness.setHand(player1, List.of(new MagnifyingGlass()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenControllerCastsNonartifactSpell() {
        addCreatureReady(player1, new ForensicGadgeteer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void reducesActivatedAbilitiesOfArtifactsYouControl() {
        addCreatureReady(player1, new ForensicGadgeteer());
        Permanent glass = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(glass), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceActivatedAbilitiesOfArtifactsAnOpponentControls() {
        addCreatureReady(player1, new ForensicGadgeteer());
        Permanent glass = harness.addToBattlefieldAndReturn(player2, new MagnifyingGlass());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(glass), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
