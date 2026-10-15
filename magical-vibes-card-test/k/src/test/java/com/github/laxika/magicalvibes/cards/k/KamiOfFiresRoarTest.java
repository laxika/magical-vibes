package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfFiresRoar.class, DampenThought.class, IsamaruHoundOfKonda.class})
class KamiOfFiresRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell makes target creature unable to block")
    void spiritCastStopsBlocking() {
        harness.addToBattlefield(player1, new KamiOfFiresRoar());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new KamiOfFiresRoar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Casting an Arcane spell makes target creature unable to block")
    void arcaneCastStopsBlocking() {
        harness.addToBattlefield(player1, new KamiOfFiresRoar());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The affected creature cannot be declared as a blocker")
    void affectedCreatureCannotBlock() {
        harness.addToBattlefield(player1, new KamiOfFiresRoar());
        addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KamiOfFiresRoar());
        addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new IsamaruHoundOfKonda()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Spirit or Arcane spell does not trigger")
    void opponentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new KamiOfFiresRoar());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player2, List.of(new DampenThought()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The can't-block restriction wears off at end of turn")
    void cantBlockWearsOff() {
        harness.addToBattlefield(player1, new KamiOfFiresRoar());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The trigger can target its own source and resolves before the Spirit spell")
    void canTargetSourceBeforeSpiritResolves() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfFiresRoar());
        harness.setHand(player1, List.of(new KamiOfFiresRoar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, kami.getId());

        assertThat(kami.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(kami.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Kami of Fire's Roar")).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Kami of Fire's Roar")).isEqualTo(2);
        assertThat(findPermanents(player1, "Kami of Fire's Roar"))
                .filteredOn(permanent -> !permanent.getId().equals(kami.getId()))
                .allMatch(permanent -> !permanent.isCantBlockThisTurn());
    }

    @Test
    @DisplayName("Kami does not trigger from its own casting while it is not on the battlefield")
    void ownCastingDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new KamiOfFiresRoar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
        harness.assertOnBattlefield(player1, "Kami of Fire's Roar");
        assertThat(gd.stack).isEmpty();
    }
}
