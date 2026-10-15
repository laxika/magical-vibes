package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DetectiveOfTheMonth.class, Forest.class, GrizzlyBears.class, ControlMagic.class, TurnToFrog.class})
class DetectiveOfTheMonthTest extends BaseCardTest {

    @Test
    void secondDrawCreatesDetectiveTokenOncePerTurn() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();
        draw(player1.getId());

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Detective").getFirst().getCard().getSubtypes())
                .contains(CardSubtype.DETECTIVE);
    }

    @Test
    void detectiveCanBeBlockedWithoutCityBlessing() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        detective.setAttacking(true);

        prepareDeclareBlockers();
        assertThatCode(() -> declareBlock(blocker, detective)).doesNotThrowAnyException();
    }

    @Test
    void cityBlessingMakesOwnDetectivesUnblockable() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        detective.setAttacking(true);

        gd.playersWithCityBlessing.add(player1.getId());
        prepareDeclareBlockers();
        assertThatThrownBy(() -> declareBlock(blocker, detective))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ascendGrantsBlessingAtTenthPermanent() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    void firstDrawAndOpponentsDrawsDoNotCreateTokens() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        draw(player1.getId());
        draw(player2.getId());
        draw(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Detective")).isEmpty();
        assertThat(findPermanents(player2, "Detective")).isEmpty();
    }

    @Test
    void secondDrawOnOpponentsTurnCreatesMulticoloredTwoTwoToken() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Detective");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DETECTIVE);
        assertThat(findPermanents(player2, "Detective")).isEmpty();
    }

    @Test
    void eachCopyTriggersOnTheSecondDraw() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        addCreatureReady(player1, new DetectiveOfTheMonth());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Detective")).hasSize(2);
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        draw(player1.getId());
        draw(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new DetectiveOfTheMonth());

        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Detective")).isEmpty();
    }

    @Test
    void tokenAsTenthPermanentGrantsPersistentBlessing() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        draw(player1.getId());
        draw(player1.getId());
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        resolveAllTriggers();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Forest);
        Permanent token = findPermanent(player1, "Detective");
        token.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThatThrownBy(() -> declareBlock(blocker, token)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blessingDoesNotMakeNonDetectivesUnblockable() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        gd.playersWithCityBlessing.add(player1.getId());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        bear.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, bear)).doesNotThrowAnyException();
    }

    @Test
    void blessingDoesNotMakeOpponentsDetectivesUnblockable() {
        addCreatureReady(player1, new DetectiveOfTheMonth());
        gd.playersWithCityBlessing.add(player1.getId());
        Permanent attacker = addCreatureReady(player2, new DetectiveOfTheMonth());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    void drawTriggerSurvivesSourceLeavingButUnblockabilityDoesNot() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        draw(player1.getId());
        draw(player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(detective);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Detective");
        token.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, token)).doesNotThrowAnyException();
    }

    @Test
    void gainingControlOfDetectiveAtTenPermanentsGrantsBlessing() {
        Permanent detective = addCreatureReady(player2, new DetectiveOfTheMonth());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, detective.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(10).contains(detective);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playersWithCityBlessing).doesNotContain(player2.getId());
    }

    @Test
    void losingAbilitiesPreventsSecondDrawTrigger() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, detective.getId());
        assertThat(gqs.hasLostAllAbilities(gd, detective)).isTrue();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Detective")).isEmpty();
    }

    @Test
    void losingAscendPreventsBlessingWhenTenthPermanentEnters() {
        Permanent detective = addCreatureReady(player1, new DetectiveOfTheMonth());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, detective.getId());
        assertThat(gqs.hasLostAllAbilities(gd, detective)).isTrue();

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(10);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
