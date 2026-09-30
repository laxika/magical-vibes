package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiegeGangLieutenant.class, GrizzlyBears.class})
class SiegeGangLieutenantTest extends BaseCardTest {

    @Test
    void createsTwoHastyGoblinTokensWhileControllingCommander() {
        addCommanderAndLieutenant();

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotCreateTokensWithoutControllingCommander() {
        harness.addToBattlefield(player1, new SiegeGangLieutenant());

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void sacrificesGoblinAndDealsOneDamageToPlayer() {
        addCommanderAndLieutenant();
        advanceToBeginningOfCombat(player1);
        Permanent lieutenant = findPermanent(player1, "Siege-Gang Lieutenant");
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), null, player2.getId());
        UUID goblinId = findPermanent(player1, "Goblin").getId();
        harness.handlePermanentChosen(player1, goblinId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    private void addCommanderAndLieutenant() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new SiegeGangLieutenant());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
