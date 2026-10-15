package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.d.DisaTheRestless;
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

@CardUsed({SiegeGangLieutenant.class, DisaTheRestless.class, BoggartShenanigans.class})
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

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        addCommanderAndLieutenant();

        advanceToBeginningOfCombat(player2);

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void controllingOpponentsCommanderDoesNotEnableLieutenant() {
        Card commander = new DisaTheRestless();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new SiegeGangLieutenant());

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void losingCommanderBeforeResolutionPreventsTokens() {
        addCommanderAndLieutenant();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        Permanent commander = findPermanent(player1, "Disa the Restless");
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerBattlefields.get(player2.getId()).add(commander);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void gainingCommanderAfterCombatBeginsDoesNotCreateTrigger() {
        Card commander = new DisaTheRestless();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, new SiegeGangLieutenant());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, commander);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void tokensLoseHasteAfterTurnEnds() {
        addCommanderAndLieutenant();
        advanceToBeginningOfCombat(player1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(findPermanents(player1, "Goblin"))
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse());
    }

    @Test
    void canSacrificeItselfWithoutCommanderWhileSummoningSick() {
        harness.addToBattlefield(player1, new SiegeGangLieutenant());
        Permanent lieutenant = findPermanent(player1, "Siege-Gang Lieutenant");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), null, player2.getId());
        harness.handlePermanentChosen(player1, lieutenant.getId());
        harness.assertNotOnBattlefield(player1, "Siege-Gang Lieutenant");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Siege-Gang Lieutenant");
    }

    @Test
    void canSacrificeNoncreatureGoblinPermanent() {
        harness.addToBattlefield(player1, new SiegeGangLieutenant());
        harness.addToBattlefield(player1, new BoggartShenanigans());
        Permanent lieutenant = findPermanent(player1, "Siege-Gang Lieutenant");
        Permanent shenanigans = findPermanent(player1, "Boggart Shenanigans");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), null, player2.getId());
        harness.handlePermanentChosen(player1, shenanigans.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Siege-Gang Lieutenant");
        harness.assertLife(player2, 19);
    }

    @Test
    void canDamageCreatureBySacrificingItself() {
        harness.addToBattlefield(player1, new SiegeGangLieutenant());
        harness.addToBattlefield(player2, new DisaTheRestless());
        Permanent lieutenant = findPermanent(player1, "Siege-Gang Lieutenant");
        Permanent target = findPermanent(player2, "Disa the Restless");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), null, target.getId());
        harness.handlePermanentChosen(player1, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Disa the Restless");
    }

    @Test
    void createsTokensInEachCombatOnControllersTurn() {
        addCommanderAndLieutenant();
        advanceToBeginningOfCombat(player1);

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Goblin")).hasSize(4);
    }

    private void addCommanderAndLieutenant() {
        Card commander = new DisaTheRestless();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new SiegeGangLieutenant());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
