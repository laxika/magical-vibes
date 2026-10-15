package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({TheFantasticar.class, ReachThroughMists.class, KamiOfOldStone.class, ConsumingVortex.class})
class TheFantasticarTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell may animate The Fantasticar until end of turn")
    void noncreatureSpellAnimatesFantasticar() {
        Permanent fantasticar = addFantasticar();
        castNoncreatureSpell(true);

        assertThat(gqs.isCreature(gd, fantasticar)).isTrue();
        assertThat(gqs.isArtifact(fantasticar)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fantasticar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fantasticar)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, fantasticar)).isFalse();
    }

    @Test
    void mayDeclineAnimation() {
        Permanent fantasticar = addFantasticar();
        castNoncreatureSpell(false);

        assertThat(gqs.isCreature(gd, fantasticar)).isFalse();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void decliningFourthSpellSacrificeDoesNotOfferSacrificeOnFifthSpell() {
        Permanent fantasticar = addFantasticar();
        for (int i = 0; i < 4; i++) {
            castNoncreatureSpell(false);
        }

        assertThat(findPermanents(player1, "The Fantasticar")).containsExactly(fantasticar);
        assertThat(findPermanents(player1, "Construct")).isEmpty();

        castNoncreatureSpell(true);

        assertThat(findPermanents(player1, "The Fantasticar")).containsExactly(fantasticar);
        assertThat(gqs.isCreature(gd, fantasticar)).isTrue();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void countsSpellsCastBeforeFantasticarEnteredBattlefield() {
        for (int i = 0; i < 3; i++) {
            castNoncreatureSpell(false);
        }
        addFantasticar();

        castNoncreatureSpell(true);

        assertThat(findPermanents(player1, "The Fantasticar")).isEmpty();
        assertThat(findPermanents(player1, "Construct")).hasSize(4);
    }

    @Test
    void creatureSpellNeitherAnimatesNorCountsTowardFourthNoncreatureSpell() {
        Permanent fantasticar = addFantasticar();
        harness.castFromHand(player1, new KamiOfOldStone(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, fantasticar)).isFalse();

        for (int i = 0; i < 3; i++) {
            castNoncreatureSpell(true);
        }
        assertThat(findPermanents(player1, "The Fantasticar")).containsExactly(fantasticar);
        assertThat(findPermanents(player1, "Construct")).isEmpty();

        castNoncreatureSpell(true);
        assertThat(findPermanents(player1, "The Fantasticar")).isEmpty();
        assertThat(findPermanents(player1, "Construct")).hasSize(4);
    }

    @Test
    void opponentSpellsDoNotTriggerFantasticar() {
        Permanent fantasticar = addFantasticar();
        for (int i = 0; i < 4; i++) {
            harness.setLibrary(player2, List.of(new KamiOfOldStone()));
            harness.castFromHand(player2, new ReachThroughMists(), "{U}");
            harness.passBothPriorities();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        assertThat(gqs.isCreature(gd, fantasticar)).isFalse();
        castNoncreatureSpell(true);
        assertThat(findPermanents(player1, "The Fantasticar")).containsExactly(fantasticar);
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void fourthSpellCountResetsAndWorksDuringOpponentTurn() {
        addFantasticar();
        for (int i = 0; i < 3; i++) {
            castNoncreatureSpell(false);
        }
        harness.setLibrary(player2, List.of(new KamiOfOldStone()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        castNoncreatureSpell(true);
        assertThat(findPermanents(player1, "The Fantasticar")).hasSize(1);
        assertThat(findPermanents(player1, "Construct")).isEmpty();

        for (int i = 0; i < 3; i++) {
            castNoncreatureSpell(true);
        }
        assertThat(findPermanents(player1, "The Fantasticar")).isEmpty();
        assertThat(findPermanents(player1, "Construct")).hasSize(4);
    }

    @Test
    @DisplayName("The fourth noncreature spell can sacrifice The Fantasticar for four Constructs")
    void fourthNoncreatureSpellCreatesConstructsAfterSacrifice() {
        addFantasticar();

        for (int i = 0; i < 4; i++) {
            castNoncreatureSpell(i == 3);
        }

        assertThat(findPermanents(player1, "The Fantasticar")).isEmpty();
        List<Permanent> constructs = findPermanents(player1, "Construct");
        assertThat(constructs).hasSize(4);
        assertThat(constructs).allSatisfy(construct -> {
            assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, construct, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, construct, Keyword.HASTE)).isTrue();
        });
    }

    private Permanent addFantasticar() {
        return harness.addToBattlefieldAndReturn(player1, new TheFantasticar());
    }

    @Test
    void cannotCreateConstructsWhenFantasticarLeavesBeforeSacrificeResolves() {
        Permanent fantasticar = addFantasticar();
        for (int i = 0; i < 3; i++) {
            castNoncreatureSpell(true);
        }

        harness.setLibrary(player1, List.of(new KamiOfOldStone()));
        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, fantasticar.getId());

        harness.passBothPriorities();
        while (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "The Fantasticar")).isEmpty();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
        harness.assertInHand(player1, "The Fantasticar");
    }

    private void castNoncreatureSpell(boolean acceptAnimation) {
        harness.setLibrary(player1, List.of(new KamiOfOldStone()));
        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();
        while (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, acceptAnimation);
            harness.passBothPriorities();
        }
    }
}
