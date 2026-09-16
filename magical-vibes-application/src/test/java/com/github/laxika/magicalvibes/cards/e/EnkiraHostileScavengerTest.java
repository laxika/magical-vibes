package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EnkiraHostileScavenger.class, Bonesplitter.class, GrizzlyBears.class})
class EnkiraHostileScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two 2/2 black Zombie Walkers")
    void etbCreatesTwoWalkers() {
        harness.setHand(player1, List.of(new EnkiraHostileScavenger()));
        addManaForEnkira();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> walkers = findPermanents(player1, "Walker");
        assertThat(walkers).hasSize(2);
        assertThat(walkers).allSatisfy(walker -> {
            assertThat(walker.getCard().getPower()).isEqualTo(2);
            assertThat(walker.getCard().getToughness()).isEqualTo(2);
            assertThat(walker.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(walker.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        });
    }

    @Test
    @DisplayName("Enkira gains indestructible when it attacks with at least two Zombies")
    void attackingWithTwoZombiesGrantsIndestructible() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        addWalkerReady(player1);
        addWalkerReady(player1);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(enkira.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Enkira does not gain indestructible when fewer than two Zombies attack")
    void attackingWithFewerThanTwoZombiesDoesNotGrantIndestructible() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        addWalkerReady(player1);
        addWalkerReady(player1);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An equipped Enkira must be blocked if able")
    void equippedEnkiraMustBeBlockedIfAble() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(enkira.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        enkira.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unequipped Enkira does not require a blocker")
    void unequippedEnkiraDoesNotRequireBlocker() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        addCreatureReady(player2, new GrizzlyBears());
        enkira.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    private Permanent addWalkerReady(com.github.laxika.magicalvibes.model.Player player) {
        Card walkerCard = new Card();
        walkerCard.setName("Walker");
        walkerCard.setType(CardType.CREATURE);
        walkerCard.setPower(2);
        walkerCard.setToughness(2);
        walkerCard.setColor(CardColor.BLACK);
        walkerCard.setSubtypes(List.of(CardSubtype.ZOMBIE));
        walkerCard.setKeywords(Set.of());
        walkerCard.setToken(true);
        Permanent walker = new Permanent(walkerCard);
        walker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(walker);
        return walker;
    }

    private void addManaForEnkira() {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
    }
}
