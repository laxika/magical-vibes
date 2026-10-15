package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SandswirlWanderglyph;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableGlyphbridge.class, SandswirlWanderglyph.class, DarksteelRelic.class,
        GrizzlyBears.class, HillGiant.class})
class UnstableGlyphbridgeTest extends BaseCardTest {

    @Test
    void castChoosesPowerTwoOrLessCreatureForEachPlayerAndDestroysTheRest() {
        Permanent firstChoice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondChoice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentLargeCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castGlyphbridge();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(firstChoice.getId(), secondChoice.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(firstChoice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(firstChoice.getId())
                .doesNotContain(secondChoice.getId(), largeCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(opponentLargeCreature.getId());
    }

    @Test
    void enteringWithoutBeingCastDoesNotDestroyCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.enterBattlefieldAndReturn(player1, new UnstableGlyphbridge());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(creature.getId());
    }

    @Test
    void craftReturnsTheCardTransformed() {
        harness.addToBattlefieldAndReturn(player1, new UnstableGlyphbridge());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.findExiledCard(relic.getCard().getId())).isNotNull();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isTransformed()
                        && permanent.getCard() instanceof SandswirlWanderglyph);
    }

    @Test
    void opponentCastingDuringTheirTurnPreventsAttackingSourceController() {
        addTransformedGlyphbridge();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new DarksteelRelic()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentWhoAttackedThisControllerCannotCastSpells() {
        addTransformedGlyphbridge();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        harness.setHand(player2, List.of(new DarksteelRelic()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        GameActionAvailabilityService actionAvailability = harness.getGameActionAvailabilityService();
        assertThat(actionAvailability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    void mustChooseAnEligibleCreatureWhenSeveralAreAvailable() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castGlyphbridge();
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(first.getId());
    }

    @Test
    void controllerChoosesWhichOpposingCreatureSurvives() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGlyphbridge();
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(chosen.getId(), destroyed.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(chosen.getId()).doesNotContain(destroyed.getId());
    }

    @Test
    void soleEligibleCreaturesSurviveWithoutAChoicePrompt() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent large = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGlyphbridge();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(own.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(opposing.getId()).doesNotContain(large.getId());
    }

    @Test
    void craftCanExileAnArtifactCardFromTheGraveyard() {
        harness.addToBattlefield(player1, new UnstableGlyphbridge());
        UnstableGlyphbridge material = new UnstableGlyphbridge();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isTransformed()
                        && permanent.getCard() instanceof SandswirlWanderglyph);
    }

    @Test
    void craftCannotUseTheSourceAsItsOnlyMaterial() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UnstableGlyphbridge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(source.getId());
    }

    @Test
    void resolvedAttackRestrictionPersistsAfterWanderglyphLeaves() {
        Permanent glyph = addTransformedGlyphbridge();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new DarksteelRelic()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(glyph);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingRestrictionEndsWhenWanderglyphLeaves() {
        Permanent glyph = addTransformedGlyphbridge();
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        harness.setHand(player2, List.of(new DarksteelRelic()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.playerBattlefields.get(player1.getId()).remove(glyph);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castArtifact(player2, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Darksteel Relic");
    }

    @Test
    void craftIsNotAllowedDuringCombat() {
        harness.addToBattlefield(player1, new UnstableGlyphbridge());
        harness.addToBattlefield(player1, new UnstableGlyphbridge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void noEligibleCreaturesMeansAllCreaturesAreDestroyed() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        castGlyphbridge();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Unstable Glyphbridge");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castGlyphbridge() {
        harness.setHand(player1, List.of(new UnstableGlyphbridge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
    }

    private Permanent addTransformedGlyphbridge() {
        UnstableGlyphbridge card = new UnstableGlyphbridge();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
