package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.cards.s.SegovianLeviathan;
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

@CardUsed({Deadfall.class, CatWarriors.class, BarbaryApes.class})
class DeadfallTest extends BaseCardTest {

    @Test
    @DisplayName("Forestwalk can be blocked while Deadfall is on the battlefield")
    void forestwalkCanBeBlocked() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new Deadfall());
        Permanent attacker = addCreatureReady(player1, new CatWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Deadfall does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, basicLand(CardSubtype.ISLAND));
        harness.addToBattlefield(player2, new Deadfall());
        Permanent attacker = addWalker(player1, Keyword.ISLANDWALK);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    @DisplayName("Deadfall also permits blocking when the attacking player controls it")
    void attackingPlayerControlsDeadfall() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player1, new Deadfall());
        Permanent attacker = addCreatureReady(player1, new CatWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk prevents blocking again after Deadfall leaves the battlefield")
    void forestwalkReturnsAfterDeadfallLeaves() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        Permanent deadfall = harness.addToBattlefieldAndReturn(player2, new Deadfall());
        Permanent attacker = addCreatureReady(player1, new CatWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        gd.playerBattlefields.get(player2.getId()).remove(deadfall);
        gd.playerGraveyards.get(player2.getId()).add(deadfall.getCard());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ignoring forestwalk does not permit a ground creature to block a flying attacker")
    void flyingStillPreventsBlocking() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new Deadfall());
        Permanent attacker = addCreatureReady(player1, new CatWarriors());
        attacker.getGrantedKeywords().add(Keyword.FLYING);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(SegovianLeviathan.class)
    @DisplayName("Deadfall also ignores forestwalk granted to a creature")
    void grantedForestwalkCanBeBlocked() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new Deadfall());
        Permanent attacker = addCreatureReady(player1, new SegovianLeviathan());
        attacker.getGrantedKeywords().add(Keyword.FORESTWALK);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @CardUsed(SegovianLeviathan.class)
    @DisplayName("A creature with both forestwalk and islandwalk still cannot be blocked over an Island")
    void anotherLandwalkOnSameCreatureStillApplies() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, basicLand(CardSubtype.ISLAND));
        harness.addToBattlefield(player2, new Deadfall());
        Permanent attacker = addCreatureReady(player1, new SegovianLeviathan());
        attacker.getGrantedKeywords().add(Keyword.FORESTWALK);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
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
