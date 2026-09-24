package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnkiraHostileScavenger.class, DarksteelAxe.class, GrizzlyBears.class})
class EnkiraHostileScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Walker tokens")
    void createsTwoWalkerTokens() {
        castAndResolveEnkira();

        List<Permanent> walkers = findPermanents(player1, "Walker");
        assertThat(walkers).hasSize(2);
        assertThat(walkers).allSatisfy(walker -> {
            assertThat(walker.getCard().getPower()).isEqualTo(2);
            assertThat(walker.getCard().getToughness()).isEqualTo(2);
            assertThat(walker.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(walker.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(walker.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Must be blocked only while equipped")
    void mustBeBlockedOnlyWhileEquipped() {
        Permanent enkira = addReady(player1, new EnkiraHostileScavenger());
        Permanent axe = addReady(player1, new DarksteelAxe());
        axe.setAttachedTo(enkira.getId());
        enkira.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Unequipped Enkira may attack unblocked")
    void unequippedMayAttackUnblocked() {
        Permanent enkira = addReady(player1, new EnkiraHostileScavenger());
        enkira.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Gains indestructible when attacking with at least two Zombies")
    void gainsIndestructibleWithTwoZombies() {
        castAndResolveEnkira();
        findPermanents(player1, "Walker").forEach(walker -> walker.setSummoningSick(false));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        Permanent enkira = findPermanent(player1, "Enkira, Hostile Scavenger");
        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Needs Enkira and at least two Zombies to attack")
    void doesNotTriggerWithFewerThanTwoZombies() {
        castAndResolveEnkira();
        findPermanents(player1, "Walker").forEach(walker -> walker.setSummoningSick(false));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        Permanent enkira = findPermanent(player1, "Enkira, Hostile Scavenger");
        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        castAndResolveEnkira();
        findPermanents(player1, "Walker").forEach(walker -> walker.setSummoningSick(false));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();
        Permanent enkira = findPermanent(player1, "Enkira, Hostile Scavenger");
        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enkira, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castAndResolveEnkira() {
        harness.setHand(player1, List.of(new EnkiraHostileScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        findPermanent(player1, "Enkira, Hostile Scavenger").setSummoningSick(false);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
