package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.o.OnduCleric;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiteOfReplication.class, GiantScorpion.class, Island.class, IntoTheRoil.class, OnduCleric.class})
class RiteOfReplicationTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, creates one token copy of target creature")
    void createsOneTokenCopyWithoutKicker() {
        harness.addToBattlefield(player2, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new RiteOfReplication()));
        addMana(2);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertTokenCopies(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With kicker, creates five token copies of target creature")
    void createsFiveTokenCopiesWithKicker() {
        harness.addToBattlefield(player2, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new RiteOfReplication()));
        addMana(7);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, targetId, null);
        harness.passBothPriorities();

        assertTokenCopies(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new RiteOfReplication()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kicked copies see every other copy enter the battlefield")
    void kickedCopiesEnterSimultaneously() {
        harness.addToBattlefield(player1, new OnduCleric());
        harness.setLife(player1, 20);
        UUID targetId = harness.getPermanentId(player1, "Ondu Cleric");
        harness.setHand(player1, List.of(new RiteOfReplication()));
        addMana(7);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, targetId, null);
        resolveAllTriggers();
        int choices = 0;
        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            assertThat(choices).isLessThan(30);
            harness.handleMayAbilityChosen(player1, true);
            choices++;
            resolveAllTriggers();
        }

        assertThat(countPermanents(player1, "Ondu Cleric")).isEqualTo(6);
        assertThat(choices).isEqualTo(30);
        harness.assertLife(player1, 200);
    }

    @Test
    @DisplayName("Copies do not inherit counters, tapped state, or marked damage")
    void copiesOnlyCopiableCharacteristics() {
        harness.addToBattlefield(player2, new GiantScorpion());
        Permanent target = findPermanent(player2, "Giant Scorpion");
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setMarkedDamage(1);
        target.tap();
        harness.setHand(player1, List.of(new RiteOfReplication()));
        addMana(2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertTokenCopies(1);
        Permanent token = findPermanent(player1, "Giant Scorpion");
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.getMarkedDamage()).isZero();
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A kicked spell creates no tokens if its target leaves before resolution")
    void kickedSpellDoesNotCopyMissingTarget() {
        harness.addToBattlefield(player2, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new RiteOfReplication()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        addMana(7);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, targetId, null);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Giant Scorpion");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana(int genericMana) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
    }

    private void assertTokenCopies(int expectedCount) {
        List<Permanent> tokens = findPermanents(player1, "Giant Scorpion").stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(expectedCount);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
        });
    }
}
