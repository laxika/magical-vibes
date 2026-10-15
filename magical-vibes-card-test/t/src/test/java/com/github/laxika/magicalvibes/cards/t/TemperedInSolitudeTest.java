package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PapercraftDecoy;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemperedInSolitude.class, Forest.class, PapercraftDecoy.class})
class TemperedInSolitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card and grants play permission when a creature attacks alone")
    void attacksAloneExilesTopCardWithPlayPermission() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent attacker = addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Does not trigger when multiple creatures attack")
    void multipleAttackersDoNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new PapercraftDecoy());
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void nonattackingCreatureDoesNotPreventTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new PapercraftDecoy());
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void opponentsLoneAttackerDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new TemperedInSolitude());
        addCreatureReady(player2, new PapercraftDecoy());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotCausePlayerToLose() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void eachCopyTriggersIndependently() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void exiledLandCanBePlayedButDoesNotGrantAdditionalLandPlay() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Forest()));
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledCreatureRequiresNormalTimingAndManaPayment() {
        PapercraftDecoy topCard = new PapercraftDecoy();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Papercraft Decoy")).isEqualTo(2);
    }

    @Test
    void unusedCardStaysExiledButPermissionExpiresWithTurn() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new PapercraftDecoy());
        harness.addToBattlefield(player1, new TemperedInSolitude());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }
}
