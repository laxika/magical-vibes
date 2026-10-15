package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronLadYoungAvenger.class, AngelsMercy.class, Divination.class, GrizzlyBears.class,
        HowlingMine.class})
class IronLadYoungAvengerTest extends BaseCardTest {

    @Test
    void reducesNoncreatureSpellsYouCast() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());
        harness.castFromHand(player1, new Divination(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesInstantSpellsYouCast() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());
        harness.castFromHand(player1, new AngelsMercy(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceCreatureSpells() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesNoncreatureArtifactSpells() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());

        harness.castFromHand(player1, new HowlingMine(), "{1}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceArtifactCreatureSpells() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());
        harness.setHand(player1, List.of(new IronLadYoungAvenger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromHand(player2, new Divination(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReplaceColoredManaWithTheReduction() {
        harness.addToBattlefield(player1, new IronLadYoungAvenger());

        assertThatThrownBy(() -> harness.castFromHand(player1, new AngelsMercy(), "{2}{W}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stopsReducingCostsAfterLeavingTheBattlefield() {
        IronLadYoungAvenger ironLad = new IronLadYoungAvenger();
        harness.addToBattlefield(player1, ironLad);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(ironLad));

        assertThatThrownBy(() -> harness.castFromHand(player1, new Divination(), "{1}{U}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flyingPreventsGroundCreaturesFromBlocking() {
        addCreatureReady(player1, new IronLadYoungAvenger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreaturesCanBlock() {
        addCreatureReady(player1, new IronLadYoungAvenger());
        Permanent blocker = addCreatureReady(player2, new IronLadYoungAvenger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
