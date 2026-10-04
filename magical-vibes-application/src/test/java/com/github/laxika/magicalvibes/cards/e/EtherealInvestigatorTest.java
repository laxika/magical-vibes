package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealInvestigator.class, Mulldrifter.class, ValMaroonedSurveyor.class, DarksteelMutation.class})
class EtherealInvestigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one Clue for each opponent")
    void investigatesForEachOpponent() {
        addThirdPlayer();

        harness.enterBattlefieldAndReturn(player1, new EtherealInvestigator());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a flying Spirit when you draw your second card each turn")
    void secondDrawCreatesSpirit() {
        harness.addToBattlefield(player1, new EtherealInvestigator());
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter(), new Mulldrifter()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Clue costs two mana and is sacrificed to draw a card")
    void clueCanDrawTheSecondCard() {
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter()));
        harness.enterBattlefieldAndReturn(player1, new EtherealInvestigator());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player2, "Clue")).isZero();

        drawCard(player1);
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Each investigation separately triggers abilities that watch investigating")
    @CardUsed({EtherealInvestigator.class, ValMaroonedSurveyor.class})
    void multipleOpponentsCauseMultipleInvestigationTriggers() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        harness.enterBattlefieldAndReturn(player1, new EtherealInvestigator());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        harness.assertLife(player3, 16);
    }

    @Test
    @DisplayName("The opponent's second draw does not create a Spirit")
    void opponentsDrawsDoNotTrigger() {
        harness.addToBattlefield(player1, new EtherealInvestigator());
        harness.setLibrary(player2, List.of(new Mulldrifter(), new Mulldrifter()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("A draw before Investigator enters still counts toward the second draw")
    void firstDrawBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter()));
        drawCard(player1);
        harness.enterBattlefieldAndReturn(player1, new EtherealInvestigator());
        resolveAllTriggers();

        drawCard(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third draw")
    void enteringAfterSecondDrawDoesNotTriggerRetroactively() {
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter(), new Mulldrifter()));
        drawCard(player1);
        drawCard(player1);
        harness.enterBattlefieldAndReturn(player1, new EtherealInvestigator());
        resolveAllTriggers();

        drawCard(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Drawing two cards from one ability creates exactly one Spirit")
    void drawingTwoCardsFromOneAbilityTriggersOnce() {
        harness.addToBattlefield(player1, new EtherealInvestigator());
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter()));

        harness.enterBattlefieldAndReturn(player1, new Mulldrifter());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw count resets and Investigator triggers during an opponent's turn")
    void triggersAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new EtherealInvestigator());
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter(),
                new Mulldrifter(), new Mulldrifter()));
        harness.setLibrary(player2, List.of(new Mulldrifter(), new Mulldrifter()));
        drawCard(player1);
        drawCard(player1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        drawCard(player1);
        assertThat(gd.stack).isEmpty();
        drawCard(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Investigator cannot trigger after Darksteel Mutation removes its abilities")
    @CardUsed({EtherealInvestigator.class, Mulldrifter.class, DarksteelMutation.class})
    void losingAbilitiesPreventsSecondDrawTrigger() {
        Permanent investigator = harness.addToBattlefieldAndReturn(player1, new EtherealInvestigator());
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Mulldrifter()));
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, investigator.getId());
        resolveAllTriggers();
        assertThat(gqs.hasLostAllAbilities(gd, investigator)).isTrue();

        drawCard(player1);
        drawCard(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
