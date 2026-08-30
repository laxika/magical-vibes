package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RimeDryad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hammerheim.class, Forest.class, GrizzlyBears.class, RimeDryad.class})
class HammerheimTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hammerheim));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes every basic landwalk ability until end of turn")
    void removesAllLandwalkAbilitiesUntilEndOfTurn() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent target = readyCreature(player2, allLandwalkCreature());

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            assertThat(gqs.hasKeyword(gd, target, landwalk)).isTrue();
        }

        activateRemoval(hammerheim, target);

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            assertThat(gqs.hasKeyword(gd, target, landwalk)).isFalse();
        }

        endTurn();

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            assertThat(gqs.hasKeyword(gd, target, landwalk)).isTrue();
        }
    }

    @Test
    @DisplayName("Allows a target with snow landwalk to be blocked until end of turn")
    void allowsSnowLandwalkCreatureToBeBlocked() {
        Permanent hammerheim = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        Permanent dryad = readyCreature(player1, new RimeDryad());
        dryad.setAttacking(true);
        Permanent snowForest = new Permanent(new Forest());
        TestCards.mutableCard(snowForest).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player2.getId()).add(snowForest);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        activateRemoval(hammerheim, dryad);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void activateRemoval(Permanent hammerheim, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hammerheim), 1, null, target.getId());
        harness.passBothPriorities();
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private static Card allLandwalkCreature() {
        Card card = new Card();
        card.setName("Test Landwalker");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(EnumSet.copyOf(Keyword.LANDWALK_MAP.keySet()));
        return card;
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
