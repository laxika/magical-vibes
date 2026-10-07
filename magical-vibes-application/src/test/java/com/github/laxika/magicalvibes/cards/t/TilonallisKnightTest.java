package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TilonallisKnight.class, ColossalDreadmaw.class, PerilousVoyage.class})
class TilonallisKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when attacking while controlling a Dinosaur")
    void boostsOnAttackWithDinosaur() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(1);
        assertThat(knight.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking without a Dinosaur does NOT trigger the ability")
    void noTriggerWithoutDinosaur() {
        addCreatureReady(player1, new TilonallisKnight());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tilonalli's Knight"));
    }

    @Test
    @DisplayName("No boost when attacking without a Dinosaur")
    void noBoostWithoutDinosaur() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's Dinosaur does not trigger the ability")
    void opponentDinosaurDoesNotCount() {
        addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Tilonalli's Knight"));
    }

    @Test
    @DisplayName("No boost if the last Dinosaur leaves before the trigger resolves")
    void noBoostIfDinosaurLeavesInResponse() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        Permanent dinosaur = addCreatureReady(player1, new ColossalDreadmaw());
        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, dinosaur.getId());
        harness.assertInHand(player1, "Colossal Dreadmaw");
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple Dinosaurs still give only one +1/+1 boost")
    void multipleDinosaursGiveOnlyOneBoost() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, new ColossalDreadmaw());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(1);
        assertThat(knight.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Dinosaur attacking does not trigger a Knight that did not attack")
    void noBoostWhenOnlyDinosaurAttacks() {
        Permanent knight = addCreatureReady(player1, new TilonallisKnight());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }
}
