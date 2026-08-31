package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({VenomConnoisseur.class, GrizzlyBears.class})
class VenomConnoisseurTest extends BaseCardTest {

    @Test
    @DisplayName("The first Alliance resolution gives Venom Connoisseur deathtouch")
    void firstResolutionGivesSelfDeathtouch() {
        Permanent connoisseur = addConnoisseur();
        Permanent existingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castCreatureAndResolveTrigger();

        Permanent enteringCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, enteringCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The second Alliance resolution gives deathtouch to all creatures you control")
    void secondResolutionGivesAllOwnCreaturesDeathtouch() {
        Permanent connoisseur = addConnoisseur();
        Permanent existingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();

        List<Permanent> ownCreatures = gd.playerBattlefields.get(player1.getId());
        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(ownCreatures.stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
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
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, connoisseur, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addConnoisseur() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new VenomConnoisseur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return connoisseur;
    }

    private void castCreatureAndResolveTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
