package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.r.RighteousAvengers;
import com.github.laxika.magicalvibes.cards.s.SegovianLeviathan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordMagnus.class, RighteousAvengers.class, CatWarriors.class, SegovianLeviathan.class,
        DurkwoodBoars.class})
class LordMagnusTest extends BaseCardTest {

    @Test
    @DisplayName("Plainswalk can be blocked while Lord Magnus is on the battlefield")
    void plainswalkCanBeBlocked() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.PLAINS));
        harness.addToBattlefield(player2, new LordMagnus());
        Permanent attacker = addAttacker(new RighteousAvengers());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk can be blocked while Lord Magnus is on the battlefield")
    void forestwalkCanBeBlocked() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new LordMagnus());
        Permanent attacker = addAttacker(new CatWarriors());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lord Magnus does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.ISLAND));
        harness.addToBattlefield(player2, new LordMagnus());
        Permanent attacker = addAttacker(new SegovianLeviathan());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lord Magnus affects landwalkers even when controlled by the attacking player")
    void landwalkCanBeBlockedWhenAttackerControlsLordMagnus() {
        harness.addToBattlefield(player2, basicLand(CardSubtype.PLAINS));
        harness.addToBattlefield(player1, new LordMagnus());
        Permanent attacker = addAttacker(new RighteousAvengers());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lord Magnus's first strike lets it survive combat with a four-power blocker")
    void firstStrikeLetsLordMagnusSurviveCombat() {
        Permanent lordMagnus = addCreatureReady(player1, new LordMagnus());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(lordMagnus)));
        declareBlock(blocker, lordMagnus);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lord Magnus");
        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addAttacker(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
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
