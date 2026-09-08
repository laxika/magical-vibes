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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.addToBattlefield(player1, new UnstableGlyphbridge());

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

        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
