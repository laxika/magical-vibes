package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.m.MidnightGuard;
import com.github.laxika.magicalvibes.cards.t.ThrabenHeretic;
import com.github.laxika.magicalvibes.cards.v.VaultOfTheArchangel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousDemon.class, ThrabenHeretic.class, MidnightGuard.class, BlackCat.class, VaultOfTheArchangel.class})
class RavenousDemonTest extends BaseCardTest {

    

    @Test
    @DisplayName("Front face does not trigger during upkeep")
    void frontFaceDoesNotTriggerDuringUpkeep() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(demon.isTransformed()).isFalse();
        assertThat(demon.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Thraben Heretic");
    }

    @Test
    @DisplayName("Activating front face sacrifices the only Human and transforms")
    void activatingSacrificesOnlyHumanAndTransforms() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.isTransformed()).isTrue();
        harness.assertInGraveyard(player1, "Thraben Heretic");
    }

    @Test
    @DisplayName("Front activated ability ignores non-Human creatures")
    void frontActivationIgnoresNonHumanCreatures() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        harness.addToBattlefieldAndReturn(player1, new BlackCat());
        forceSorcerySpeed(player1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(demon.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Black Cat");
    }

    @Test
    @DisplayName("Front activated ability prompts when multiple Humans are available")
    void frontActivationPromptsForMultipleHumans() {
        harness.addToBattlefield(player1, new RavenousDemon());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MidnightGuard());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BlackCat());
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId(), second.getId())
                .doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("Chosen Human is sacrificed as activation cost and Ravenous Demon transforms")
    void chosenHumanActivationCostTransforms() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        harness.addToBattlefieldAndReturn(player1, new MidnightGuard());
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(demon.isTransformed()).isTrue();
        harness.assertInGraveyard(player1, "Thraben Heretic");
        harness.assertOnBattlefield(player1, "Midnight Guard");
    }

    @Test
    @DisplayName("Archdemon taps and deals 9 damage to controller when no Human is available")
    void archdemonTapsAndDealsDamageWithoutHuman() {
        Permanent archdemon = addTransformedArchdemon(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archdemon.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 9);
    }

    @Test
    @DisplayName("Archdemon sacrifices a Human without transforming back or dealing damage")
    void archdemonSacrificesHumanWithoutTransformingBack() {
        Permanent archdemon = addTransformedArchdemon(player1);
        harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archdemon.isTransformed()).isTrue();
        assertThat(archdemon.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Thraben Heretic");
    }

    @Test
    @DisplayName("Archdemon prompts for a Human when multiple Humans are available")
    void archdemonPromptsForMultipleHumans() {
        addTransformedArchdemon(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MidnightGuard());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BlackCat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.ForcedCostOrElse.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId(), second.getId())
                .doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("Human is sacrificed before the transformation ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        harness.addToBattlefield(player1, new ThrabenHeretic());
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Thraben Heretic");
        assertThat(demon.isTransformed()).isFalse();
        harness.passBothPriorities();
        assertThat(demon.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Front activation is prohibited during upkeep")
    void activationRequiresSorceryTiming() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        harness.addToBattlefield(player1, new ThrabenHeretic());
        advanceToUpkeep(player1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(demon.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Thraben Heretic");
    }

    @Test
    @DisplayName("Opponent's Human cannot pay the front activation cost")
    void cannotSacrificeOpponentsHuman() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        harness.addToBattlefield(player2, new ThrabenHeretic());
        forceSorcerySpeed(player1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(demon.isTransformed()).isFalse();
        harness.assertOnBattlefield(player2, "Thraben Heretic");
    }

    @Test
    @DisplayName("Back face does not trigger during opponent's upkeep")
    void backFaceIgnoresOpponentsUpkeep() {
        Permanent archdemon = addTransformedArchdemon(player1);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(archdemon.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent's Human and a controlled non-Human do not prevent the penalty")
    void upkeepRequiresControlledHuman() {
        Permanent archdemon = addTransformedArchdemon(player1);
        harness.addToBattlefield(player1, new BlackCat());
        harness.addToBattlefield(player2, new ThrabenHeretic());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archdemon.isTapped()).isTrue();
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Black Cat");
        harness.assertOnBattlefield(player2, "Thraben Heretic");
    }

    @Test
    @DisplayName("Already tapped back face still deals damage when no Human is available")
    void tappedBackFaceStillDealsDamage() {
        Permanent archdemon = addTransformedArchdemon(player1);
        advanceToUpkeep(player1);
        archdemon.setTapped(true);
        harness.passBothPriorities();

        assertThat(archdemon.isTapped()).isTrue();
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Chosen upkeep Human is sacrificed and the other Human survives")
    void chosenUpkeepHumanAvoidsPenalty() {
        Permanent archdemon = addTransformedArchdemon(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ThrabenHeretic());
        harness.addToBattlefield(player1, new MidnightGuard());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, human.getId());

        harness.assertInGraveyard(player1, "Thraben Heretic");
        harness.assertOnBattlefield(player1, "Midnight Guard");
        harness.assertLife(player1, 20);
        assertThat(archdemon.isTapped()).isFalse();
        assertThat(archdemon.isTransformed()).isTrue();
    }
    @Test
    @DisplayName("Lifelink gained in response to upkeep offsets the back face's damage")
    void upkeepDamageAppliesGrantedLifelink() {
        Permanent archdemon = addTransformedArchdemon(player1);
        harness.addToBattlefield(player1, new VaultOfTheArchangel());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(archdemon.isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }
    private void forceSorcerySpeed(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addTransformedArchdemon(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RavenousDemon());
        permanent.setCard(permanent.getOriginalCard().getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        return permanent;
    }

}
