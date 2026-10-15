package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenevolentBodyguard.class, CabalTrainee.class, DefyGravity.class, KrosanVerge.class})
class DefyGravityTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent bodyguard = harness.addToBattlefieldAndReturn(player1, new BenevolentBodyguard());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = bodyguard.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Target creature an opponent controls gains flying")
    void grantsFlyingToOpponentsCreature() {
        Permanent bodyguard = harness.addToBattlefieldAndReturn(player2, new BenevolentBodyguard());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = bodyguard.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flashback grants flying and exiles Defy Gravity")
    void flashbackGrantsFlyingAndExilesSpell() {
        Permanent bodyguard = harness.addToBattlefieldAndReturn(player1, new BenevolentBodyguard());
        harness.setGraveyard(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = bodyguard.getId();
        harness.castAndResolveFlashback(player1, 0, targetId);

        assertThat(bodyguard.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Defy Gravity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Defy Gravity"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new BenevolentBodyguard());
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Krosan Verge");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CabalTrainee());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A normally cast Defy Gravity can be flashed back after cleanup")
    void normalCastCanBeFlashedBackAfterCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CabalTrainee());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Defy Gravity");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveFlashback(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Defy Gravity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Defy Gravity"));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its target leaves the battlefield")
    void flashbackExilesSpellWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CabalTrainee());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new BenevolentBodyguard());
        harness.setGraveyard(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.activateAbility(player1, 0, null, survivor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cabal Trainee");
        assertThat(survivor.hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertNotInGraveyard(player1, "Defy Gravity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Defy Gravity"));
        assertThat(gd.stack).isEmpty();
    }
}
