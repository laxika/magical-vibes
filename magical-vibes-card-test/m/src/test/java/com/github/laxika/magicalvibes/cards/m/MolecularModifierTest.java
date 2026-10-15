package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MolecularModifier.class, GrizzlyBears.class, Telepathy.class, Shock.class})
class MolecularModifierTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, gives a creature you control +1/+0 and first strike")
    void boostsTargetCreatureAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new MolecularModifier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The trigger targets only creatures controlled by Molecular Modifier's controller")
    void targetsOnlyCreatureYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MolecularModifier());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(source.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("The trigger rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new MolecularModifier());
        Permanent telepathy = harness.addToBattlefieldAndReturn(player1, new Telepathy());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, telepathy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MolecularModifier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new MolecularModifier());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Molecular Modifier can target itself")
    void canTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MolecularModifier());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability resolves even if Molecular Modifier leaves the battlefield")
    void resolvesAfterSourceIsRemoved() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MolecularModifier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability does not affect another creature if its target is removed")
    void doesNotRetargetWhenTargetIsRemoved() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MolecularModifier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
