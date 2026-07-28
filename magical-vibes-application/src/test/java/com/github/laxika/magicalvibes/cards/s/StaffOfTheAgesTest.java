package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RimeDryad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaffOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Without the Staff, a forestwalking attacker can't be blocked through a Forest")
    void forestwalkStopsBlockWithoutStaff() {
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = addForestwalker(player1);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        beginBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With the Staff out, a forestwalking attacker can be blocked through a Forest")
    void staffLetsForestwalkerBeBlocked() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent attacker = addForestwalker(player1);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        beginBlockers();
        declareBlock(blocker, attacker);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Staff also switches off snow landwalk")
    void staffSwitchesOffSnowLandwalk() {
        addSnowForest(player2);
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent dryad = readyCreature(player1, new RimeDryad());
        dryad.setAttacking(true);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        beginBlockers();
        declareBlock(blocker, dryad);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Staff leaves non-landwalk evasion alone")
    void staffDoesNotAffectFlying() {
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Card flyer = createCreature("Test Flyer", 2, 2, CardColor.WHITE);
        flyer.setKeywords(EnumSet.of(Keyword.FLYING));
        Permanent attacker = readyCreature(player1, flyer);
        attacker.setAttacking(true);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        beginBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addForestwalker(Player player) {
        Card card = createCreature("Test Forestwalker", 2, 2, CardColor.GREEN);
        card.setKeywords(EnumSet.of(Keyword.FORESTWALK));
        Permanent perm = readyCreature(player, card);
        perm.setAttacking(true);
        return perm;
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addSnowForest(Player player) {
        Permanent snowForest = new Permanent(new Forest());
        TestCards.mutableCard(snowForest).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowForest);
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
