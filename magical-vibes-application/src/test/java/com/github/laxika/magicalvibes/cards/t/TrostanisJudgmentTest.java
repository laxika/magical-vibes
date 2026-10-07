package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmoryGuard;
import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrostanisJudgment.class, ArmoryGuard.class, CallOfTheConclave.class, Card.class})
class TrostanisJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the targeted creature and populates the controller's only creature token")
    void exilesTargetAndPopulates() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArmoryGuard());
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new TrostanisJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles the target even when there is no creature token to populate")
    void exilesWithoutTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArmoryGuard());
        harness.setHand(player1, List.of(new TrostanisJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal sole target prevents populate as well as exile")
    void doesNotPopulateWhenTargetLeavesBattlefield() {
        createCentaur(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoryGuard());
        harness.setHand(player1, List.of(new TrostanisJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Trostani's Judgment");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiling your only creature token leaves nothing to populate")
    void exilesOwnTokenBeforePopulating() {
        createCentaur(player1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new TrostanisJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Populate cannot copy an opponent's token or your nontoken creature")
    void ignoresOpposingTokensAndOwnNontokens() {
        createCentaur(player2);
        harness.addToBattlefield(player1, new ArmoryGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoryGuard());
        harness.setHand(player1, List.of(new TrostanisJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card()).isSameAs(target.getCard()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exile completes before the controller chooses which token to populate")
    void choosesAmongMultipleTokensAndFinishesResolution() {
        createCentaur(player1);
        Permanent centaur = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, soldierToken());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoryGuard());
        harness.setHand(player1, List.of(new TrostanisJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, centaur.getId());

        assertThat(countPermanents(player1, centaur.getCard().getName())).isEqualTo(2);
        assertThat(countPermanents(player1, "Soldier Token")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Trostani's Judgment");
    }

    private void createCentaur(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new CallOfTheConclave()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player, 0, 0);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
