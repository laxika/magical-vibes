package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LiesaForgottenArchangel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({GisaGloriousResurrector.class, GrizzlyBears.class, Shock.class, LiesaForgottenArchangel.class,
        TurnToFrog.class, GraveyardTrespasser.class, GraveyardGlutton.class})
class GisaGloriousResurrectorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's dying creature with Gisa")
    void exilesOpponentCreatureWithGisa() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyCreature(player1, bears.getId());

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getCard().getId()).sourcePermanentId())
                .isEqualTo(gisa.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns exiled creatures under Gisa's control with decayed at upkeep")
    void returnsExiledCreatureWithDecayedAtUpkeep() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, bears.getId());

        triggerUpkeep(player1);

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        assertThat(returned).isNotNull();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DECAYED)).isTrue();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
        assertThat(gisa.getId()).isNotEqualTo(returned.getId());
    }

    @Test
    @DisplayName("Does not replace the controller's own dying creature")
    void doesNotReplaceOwnCreatureDeath() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyCreature(player2, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    void returnsAllExiledCreatures() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, first.getId());
        destroyCreature(player1, second.getId());

        triggerUpkeep(player1);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allSatisfy(permanent -> assertThat(gqs.hasKeyword(gd, permanent, Keyword.DECAYED)).isTrue());
        assertThat(gd.findExiledCard(first.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(second.getCard().getId())).isNull();
    }

    @Test
    void doesNotReturnCreaturesDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, bears.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void resolvesReturnAfterGisaDiesInResponse() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, bears.getId());
        advanceToUpkeep(player1);
        destroyCreature(player2, gisa.getId());
        destroyCreature(player2, gisa.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gisa, Glorious Resurrector");
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gqs.hasKeyword(gd, findPermanents(player1, "Grizzly Bears").getFirst(), Keyword.DECAYED)).isTrue();
    }

    @Test
    void newGisaDoesNotReturnCardsExiledByPreviousGisa() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, bears.getId());
        destroyCreature(player2, gisa.getId());
        destroyCreature(player2, gisa.getId());
        harness.addToBattlefield(player1, new GisaGloriousResurrector());

        triggerUpkeep(player1);

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @CardUsed(TurnToFrog.class)
    void losingAbilitiesDisablesExileReplacement() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, gisa.getId());

        destroyCreature(player2, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    @CardUsed(LiesaForgottenArchangel.class)
    void opposingControllerChoosesBetweenCompetingExileReplacements() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyCreature(player2, bears.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void returnedAttackerIsSacrificedAtEndOfCombat() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, bears.getId());
        triggerUpkeep(player1);
        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);

        declareAttackers(List.of(1));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    @CardUsed({GraveyardTrespasser.class, GraveyardGlutton.class})
    void decayedTriggersAlongsidePrintedAttackAbility() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        Permanent trespasser = harness.addToBattlefieldAndReturn(player2, new GraveyardTrespasser());
        destroyCreature(player2, trespasser.getId());
        destroyCreature(player2, trespasser.getId());
        triggerUpkeep(player1);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMultipleCardsChosen(player1, List.of());
            resolveAllTriggers();
        }
        Permanent returned = findPermanents(player1, "Graveyard Trespasser").getFirst();
        returned.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).hasSize(2);
    }

    private void destroyCreature(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
    }
}
