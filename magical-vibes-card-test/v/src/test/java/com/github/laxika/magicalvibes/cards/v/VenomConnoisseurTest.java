package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HighRiseSawjack;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomConnoisseur.class, HighRiseSawjack.class})
class VenomConnoisseurTest extends BaseCardTest {

    @Test
    @DisplayName("The first Alliance resolution gives Venom Connoisseur deathtouch")
    void firstResolutionGivesSelfDeathtouch() {
        Permanent connoisseur = addConnoisseur();
        Permanent existingCreature = addCreatureReady(player1, new HighRiseSawjack());
        Permanent opponentCreature = addCreatureReady(player2, new HighRiseSawjack());

        castCreatureAndResolveTrigger();

        Permanent enteringCreature = findPermanents(player1, "High-Rise Sawjack").getLast();
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, enteringCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The second Alliance resolution gives deathtouch to all creatures you control")
    void secondResolutionGivesAllOwnCreaturesDeathtouch() {
        Permanent connoisseur = addConnoisseur();
        Permanent existingCreature = addCreatureReady(player1, new HighRiseSawjack());
        Permanent opponentCreature = addCreatureReady(player2, new HighRiseSawjack());

        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();

        List<Permanent> ownCreatures = gd.playerBattlefields.get(player1.getId());
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(ownCreatures.stream()
                .filter(permanent -> permanent.getCard() instanceof HighRiseSawjack)
                .allMatch(permanent -> gqs.hasKeyword(gd, permanent, Keyword.DEATHTOUCH))).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Alliance grants deathtouch only until end of turn")
    void grantWearsOffAtEndOfTurn() {
        Permanent connoisseur = addConnoisseur();

        castCreatureAndResolveTrigger();
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Alliance does not trigger for a creature entering under an opponent's control")
    void doesNotTriggerForOpponentCreature() {
        Permanent connoisseur = addConnoisseur();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HighRiseSawjack(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Venom Connoisseur does not trigger for its own entry")
    void doesNotTriggerForOwnEntry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new VenomConnoisseur(), "{1}{G}");
        harness.passBothPriorities();

        Permanent connoisseur = findPermanent(player1, "Venom Connoisseur");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The third resolution does not grant deathtouch to later creatures")
    void thirdResolutionDoesNotRepeatTeamGrant() {
        Permanent connoisseur = addConnoisseur();

        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();
        List<Permanent> earlierCreatures = findPermanents(player1, "High-Rise Sawjack");
        castCreatureAndResolveTrigger();

        Permanent laterCreature = findPermanents(player1, "High-Rise Sawjack").getLast();
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();
        assertThat(earlierCreatures).hasSize(2);
        assertThat(earlierCreatures)
                .allSatisfy(creature -> assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue());
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Each Venom Connoisseur counts its own ability resolutions")
    void separateConnoisseursDoNotShareResolutionCounts() {
        Permanent first = addConnoisseur();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VenomConnoisseur());
        Permanent existingCreature = addCreatureReady(player1, new HighRiseSawjack());

        castCreatureAndResolveTrigger();

        Permanent enteringCreature = findPermanents(player1, "High-Rise Sawjack").getLast();
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, enteringCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The team grant expires and the resolution count resets on the next turn")
    void teamGrantExpiresAndResolutionCountResets() {
        Permanent connoisseur = addConnoisseur();
        Permanent existingCreature = addCreatureReady(player1, new HighRiseSawjack());
        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();
        List<Permanent> grantedCreatures = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        assertThat(grantedCreatures)
                .allSatisfy(creature -> assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(grantedCreatures)
                .allSatisfy(creature -> assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castCreatureAndResolveTrigger();

        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanents(player1, "High-Rise Sawjack").getLast(),
                Keyword.DEATHTOUCH)).isFalse();

        castCreatureAndResolveTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(creature -> assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue());
    }

    private Permanent addConnoisseur() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new VenomConnoisseur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return connoisseur;
    }

    private void castCreatureAndResolveTrigger() {
        harness.castFromHand(player1, new HighRiseSawjack(), "{2}{G}");
        resolveAllTriggers();
    }
}
