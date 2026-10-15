package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({NephaliaMoondrakes.class, DevilthornFox.class, Forest.class})
class NephaliaMoondrakesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and grants flying to the targeted creature")
    void entersAndGrantsFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new NephaliaMoondrakes()));
        addCastMana();

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ETB flying grant wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new NephaliaMoondrakes()));
        addCastMana();

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The graveyard ability exiles itself and grants flying to your creatures")
    void graveyardAbilityExilesItselfAndGrantsFlying() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.setGraveyard(player1, List.of(new NephaliaMoondrakes()));
        addGraveyardAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotInGraveyard(player1, "Nephalia Moondrakes");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Nephalia Moondrakes"));

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ETB ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NephaliaMoondrakes()));
        addCastMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard grant expires at end of turn")
    void graveyardFlyingWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setGraveyard(player1, List.of(new NephaliaMoondrakes()));
        addGraveyardAbilityMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The graveyard ability affects creatures present at resolution")
    void creatureEnteringBeforeResolutionGainsFlying() {
        harness.setGraveyard(player1, List.of(new NephaliaMoondrakes()));
        addGraveyardAbilityMana();
        harness.activateGraveyardAbility(player1, 0);

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain flying")
    void creatureEnteringAfterResolutionDoesNotGainFlying() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setGraveyard(player1, List.of(new NephaliaMoondrakes()));
        addGraveyardAbilityMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Insufficient mana prevents activation and leaves the card in the graveyard")
    void insufficientManaDoesNotExileSource() {
        harness.setGraveyard(player1, List.of(new NephaliaMoondrakes()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Nephalia Moondrakes"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void addGraveyardAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
