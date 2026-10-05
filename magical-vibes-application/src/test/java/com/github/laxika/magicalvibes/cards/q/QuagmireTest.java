package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagicalHack;
import com.github.laxika.magicalvibes.cards.s.ShanodinDryads;
import com.github.laxika.magicalvibes.cards.s.SolkanarTheSwampKing;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quagmire.class, Swamp.class, Forest.class, GrizzlyBears.class,
        SolkanarTheSwampKing.class, MagicalHack.class, ShanodinDryads.class})
class QuagmireTest extends BaseCardTest {

    @Test
    @DisplayName("Swampwalk can be blocked while Quagmire is on the battlefield")
    void swampwalkCanBeBlocked() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Quagmire());
        Permanent attacker = addWalker(player1, Keyword.SWAMPWALK);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Quagmire does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Quagmire());
        Permanent attacker = addWalker(player1, Keyword.FORESTWALK);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Quagmire works even when controlled by the attacking player")
    void swampwalkCanBeBlockedWhenAttackerControlsQuagmire() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player1, new Quagmire());
        Permanent attacker = addWalker(player1, Keyword.SWAMPWALK);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Quagmire permits blocking printed swampwalk without removing the ability")
    void printedSwampwalkRemainsAnAbility() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Quagmire());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking again after Quagmire leaves the battlefield")
    void swampwalkResumesWhenQuagmireLeaves() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent quagmire = harness.addToBattlefieldAndReturn(player2, new Quagmire());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(quagmire);
        gd.playerGraveyards.get(player2.getId()).add(quagmire.getCard());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changing Quagmire's Swamp text to Forest permits blocking forestwalk")
    void changedTextPermitsForestwalkBlocking() {
        harness.addToBattlefield(player2, new Forest());
        Permanent quagmire = harness.addToBattlefieldAndReturn(player2, new Quagmire());
        Permanent attacker = addCreatureReady(player1, new ShanodinDryads());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        changeSwampToForest(quagmire);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Changing Quagmire's Swamp text to Forest stops permitting swampwalk blocks")
    void changedTextNoLongerPermitsSwampwalkBlocking() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent quagmire = harness.addToBattlefieldAndReturn(player2, new Quagmire());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        changeSwampToForest(quagmire);
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void changeSwampToForest(Permanent quagmire) {
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, quagmire.getId());
        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");
        resolveAllTriggers();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addWalker(Player player, Keyword landwalk) {
        Card card = new Card();
        card.setName("Test Walker");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(EnumSet.of(landwalk));
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
