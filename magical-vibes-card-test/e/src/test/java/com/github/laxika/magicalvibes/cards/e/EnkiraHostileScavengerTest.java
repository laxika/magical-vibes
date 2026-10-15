package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnkiraHostileScavenger.class, GrizzlyBears.class, Bonesplitter.class})
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

    @Test
    @DisplayName("Two attacking Zombies do not grant indestructible to Enkira that stays back")
    void zombiesAttackingWithoutEnkiraDoNotTrigger() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        addWalkerReady(player1);
        addWalkerReady(player1);

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger still grants indestructible after a Zombie leaves combat")
    void zombieLeavingCombatDoesNotInvalidateTrigger() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        Permanent walker = addWalkerReady(player1);
        addWalkerReady(player1);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1, 2)));
        assertThat(gd.stack).hasSize(1);
        walker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An equipped Enkira may remain unblocked when the defender has no creatures")
    void equippedEnkiraWithNoAvailableBlockers() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(enkira.getId());
        enkira.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Indestructible applies only to Enkira and expires at the end of the turn")
    void indestructibleIsSelfOnlyAndExpires() {
        Permanent enkira = addCreatureReady(player1, new EnkiraHostileScavenger());
        Permanent firstWalker = addWalkerReady(player1);
        Permanent secondWalker = addWalkerReady(player1);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstWalker, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondWalker, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passUntil(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isFalse();
    }
    private Permanent addWalkerReady(Player player) {
        Card walkerCard = new Card();
        walkerCard.setName("Walker");
        walkerCard.setType(CardType.CREATURE);
        walkerCard.setPower(2);
        walkerCard.setToughness(2);
        walkerCard.setColor(CardColor.BLACK);
        walkerCard.setSubtypes(List.of(CardSubtype.ZOMBIE));
        walkerCard.setKeywords(Set.of());
        walkerCard.setToken(true);
        return addCreatureReady(player, walkerCard);
    }

    private void addManaForEnkira() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
