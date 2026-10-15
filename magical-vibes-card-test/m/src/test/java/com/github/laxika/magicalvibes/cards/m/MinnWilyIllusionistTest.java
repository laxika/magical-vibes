package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Weakness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinnWilyIllusionist.class, BlindPhantasm.class, GrizzlyBears.class, Forest.class, Shock.class,
        MaskwoodNexus.class, Weakness.class})
class MinnWilyIllusionistTest extends BaseCardTest {

    @Test
    void secondDrawCreatesIllusionsThatScaleWithOtherIllusions() {
        harness.addToBattlefieldAndReturn(player1, new MinnWilyIllusionist());
        harness.addToBattlefield(player1, new BlindPhantasm());
        addCardsToDeck(2);

        draw();
        assertThat(gd.stack).isEmpty();
        draw();
        resolveAllTriggers();

        List<Permanent> illusions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(illusions).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, illusions.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, illusions.getFirst())).isEqualTo(1);
    }

    @Test
    void dyingIllusionMayPutPermanentFromHandWithManaValueUpToItsPower() {
        harness.addToBattlefieldAndReturn(player1, new MinnWilyIllusionist());
        addCardsToDeck(2);
        draw();
        draw();
        resolveAllTriggers();

        Permanent illusion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        killWithShock(illusion);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandCardChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void thirdDrawDoesNotCreateAnotherTokenEvenOnOpponentsTurn() {
        harness.addToBattlefield(player1, new MinnWilyIllusionist());
        harness.forceActivePlayer(player2);
        addCardsToDeck(3);

        draw();
        draw();
        resolveAllTriggers();
        draw();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    void mayDeclinePuttingAnEligiblePermanentOntoBattlefield() {
        harness.addToBattlefield(player1, new MinnWilyIllusionist());
        Permanent illusion = createIllusion();
        harness.setHand(player1, List.of(new Forest()));

        killWithShock(illusion);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void usesBoostedPowerBeforeDeathAndAllowsEqualManaValue() {
        harness.addToBattlefield(player1, new MinnWilyIllusionist());
        harness.addToBattlefield(player1, new BlindPhantasm());
        Permanent illusion = createIllusion();
        harness.setHand(player1, List.of(new GrizzlyBears(), new BlindPhantasm(), new Shock()));

        killWithShock(illusion);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandCardChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opposingIllusionAndOwnNonIllusionDoNotTrigger() {
        harness.addToBattlefield(player1, new MinnWilyIllusionist());
        Permanent opposingIllusion = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        killWithShock(opposingIllusion);
        killWithShock(opposingIllusion);
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        killWithShock(bear);
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void minnTriggersOnItsOwnDeathWhenItIsAnIllusion() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent minn = harness.addToBattlefieldAndReturn(player1, new MinnWilyIllusionist());
        harness.setHand(player1, List.of(new Forest()));

        killWithShock(minn);
        killWithShock(minn);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void negativePowerIllusionCannotPutEvenALandOntoBattlefield() {
        harness.addToBattlefield(player1, new MinnWilyIllusionist());
        Permanent illusion = createIllusion();
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Weakness()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castEnchantment(player2, 0, illusion.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    private Permanent createIllusion() {
        addCardsToDeck(2);
        draw();
        draw();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void addCardsToDeck(int count) {
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
