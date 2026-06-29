package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NiblisOfTheMistTest extends BaseCardTest {

    @Test
    @DisplayName("Has optional ETB TapTargetPermanentEffect")
    void hasOptionalEtbTapEffect() {
        NiblisOfTheMist card = new NiblisOfTheMist();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(TapTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Resolving ETB trigger prompts for may choice")
    void resolvingEtbPromptsForMayChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castNiblis();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting ETB may taps target opponent creature")
    void acceptingMayTapsOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        castNiblisAndAcceptTarget(bears.getId());

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting ETB may can tap own creature")
    void acceptingMayCanTapOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();

        castNiblisAndAcceptTarget(bears.getId());

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining ETB may leaves creature untapped")
    void decliningMayLeavesCreatureUntapped() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        castNiblis();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Niblis of the Mist");
    }

    @Test
    @DisplayName("Accepting ETB may prompts for a creature target and rejects noncreatures")
    void acceptingMayPromptsForCreatureTargetAndRejectsNoncreature() {
        harness.addToBattlefield(player2, new Telepathy());
        UUID telepathyId = harness.getPermanentId(player2, "Telepathy");

        castNiblis();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, telepathyId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.assertOnBattlefield(player2, "Telepathy");
    }

    private void castNiblisAndAcceptTarget(UUID targetId) {
        castNiblis();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
    }

    private void castNiblis() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NiblisOfTheMist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
