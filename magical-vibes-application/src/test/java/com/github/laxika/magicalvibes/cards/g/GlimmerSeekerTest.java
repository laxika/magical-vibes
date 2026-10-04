package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnduringInnocence;
import com.github.laxika.magicalvibes.cards.p.PatchworkBeastie;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimmerSeeker.class, PatchworkBeastie.class, EnduringInnocence.class, RelentlessAssault.class})
class GlimmerSeekerTest extends BaseCardTest {

    @Test
    void createsGlimmerTokenWithoutControlledGlimmerCreature() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmer");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsCardWithControlledGlimmerCreature() {
        addGlimmerCreature();
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Patchwork Beastie");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GLIMMER))
                .hasSize(1);
    }

    @Test
    void doesNotTriggerWhenUntapped() {
        harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGlimmerCreature() {
        return harness.addToBattlefieldAndReturn(player1, new EnduringInnocence());
    }

    @Test
    void doesNothingIfUntappedBeforeResolution() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        advanceToPostcombatMain();
        assertThat(gd.stack).hasSize(1);

        seeker.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glimmer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void usesTappedStatusWhenSourceLeavesBeforeResolution() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        advanceToPostcombatMain();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, seeker);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmer");
        harness.assertInGraveyard(player1, "Glimmer Seeker");
    }

    @Test
    void usesUntappedStatusWhenSourceUntapsAndLeavesBeforeResolution() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        advanceToPostcombatMain();

        seeker.untap();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, seeker);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glimmer");
    }

    @Test
    void opponentsGlimmerDoesNotEnableDrawing() {
        harness.addToBattlefield(player2, new EnduringInnocence());
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmer");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsIfGlimmerEntersBeforeResolution() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));
        advanceToPostcombatMain();

        addGlimmerCreature();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Patchwork Beastie");
        harness.assertNotOnBattlefield(player1, "Glimmer");
    }

    @Test
    void createsTokenIfLastGlimmerLeavesBeforeResolution() {
        Permanent glimmer = addGlimmerCreature();
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));
        advanceToPostcombatMain();

        harness.getPermanentRemovalService().removePermanentToHand(gd, glimmer);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmer");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsSecondMainPhase() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Glimmer");
    }

    @Test
    void doesNotTriggerDuringThirdMainPhase() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Glimmer");

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingAfterSecondMainPhaseBeginsDoesNotTrigger() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        advanceToPostcombatMain();

        seeker.tap();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Glimmer");
    }

    @Test
    void noncreatureGlimmerDoesNotEnableDrawing() {
        Permanent glimmer = addGlimmerCreature();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, glimmer);
        harness.passBothPriorities();
        Permanent returnedGlimmer = findPermanent(player1, "Enduring Innocence");
        assertThat(gqs.isCreature(gd, returnedGlimmer)).isFalse();
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmer");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void secondSeekerDrawsAfterFirstSeekerCreatesGlimmer() {
        harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker()).tap();
        harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker()).tap();
        harness.setLibrary(player1, List.of(new PatchworkBeastie()));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Patchwork Beastie");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }
}
