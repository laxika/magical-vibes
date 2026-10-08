package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.s.SolkanarTheSwampKing;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrDrago.class, BarbaryApes.class, SolkanarTheSwampKing.class, Swamp.class})
class UrDragoTest extends BaseCardTest {

    @Test
    @DisplayName("Swampwalk can be blocked while Ur-Drago is on the battlefield")
    void swampwalkCanBeBlocked() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.SWAMP));
        harness.addToBattlefield(player2, new UrDrago());
        Permanent attacker = addWalker(player1, Keyword.SWAMPWALK);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ur-Drago does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new UrDrago());
        Permanent attacker = addWalker(player1, Keyword.FORESTWALK);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ur-Drago lets a swampwalking attacker be blocked when controlled by that attacker")
    void swampwalkCanBeBlockedWhenAttackerControlsUrDrago() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.SWAMP));
        harness.addToBattlefield(player1, new UrDrago());
        Permanent attacker = addWalker(player1, Keyword.SWAMPWALK);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A real swampwalker can be blocked while Ur-Drago is on the battlefield")
    void realSwampwalkerCanBeBlocked() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new UrDrago());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking again after Ur-Drago leaves the battlefield")
    void swampwalkReturnsAfterUrDragoLeaves() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent urDrago = addCreatureReady(player2, new UrDrago());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        gd.playerBattlefields.get(player2.getId()).remove(urDrago);
        gd.playerGraveyards.get(player2.getId()).add(urDrago.getCard());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ur-Drago cannot suppress swampwalk after losing its abilities")
    void swampwalkReturnsWhenUrDragoLosesAbilities() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent urDrago = addCreatureReady(player2, new UrDrago());
        urDrago.setLosesAllAbilitiesUntilEndOfTurn(true);
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ur-Drago kills its blocker with first strike before taking combat damage")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent attacker = addCreatureReady(player1, new UrDrago());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        attacker.setMarkedDamage(3);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
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

    private Card basicLand(CardSubtype subtype) {
        Card card = new Card();
        card.setName(subtype.getDisplayName());
        card.setType(CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
