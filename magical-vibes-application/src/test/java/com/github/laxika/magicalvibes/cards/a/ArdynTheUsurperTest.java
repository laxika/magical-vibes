package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdynTheUsurper.class, DemonOfDeathsGate.class, Forest.class, GrizzlyBears.class})
class ArdynTheUsurperTest extends BaseCardTest {

    @Test
    void givesDemonsMenaceLifelinkAndHaste() {
        harness.addToBattlefield(player1, new ArdynTheUsurper());
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfDeathsGate());
        Permanent nonDemon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDemon = harness.addToBattlefieldAndReturn(player2, new DemonOfDeathsGate());

        assertThat(gqs.hasKeyword(gd, demon, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, demon, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, demon, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonDemon, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentDemon, Keyword.MENACE)).isFalse();
    }

    @Test
    void beginningOfCombatCreatesBlackFiveFiveDemonCopyFromAnyGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears, forest)));
        harness.addToBattlefield(player1, new ArdynTheUsurper());

        advanceToCombat(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void doesNotTriggerOnOpponentCombat() {
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addToBattlefield(player1, new ArdynTheUsurper());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
