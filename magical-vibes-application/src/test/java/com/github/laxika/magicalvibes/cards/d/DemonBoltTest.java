package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonBolt.class, Forest.class, GarrukWildspeaker.class, GrizzlyBears.class,
        RavenousLindwurm.class})
class DemonBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DemonBolt()));
        addDemonBoltMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 4 damage to target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new DemonBolt()));
        addDemonBoltMana();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DemonBolt()));
        addDemonBoltMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be foretold and cast on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        DemonBolt spell = new DemonBolt();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId(), harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Demon Bolt");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void dealsExactlyFourDamageToOwnSurvivingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RavenousLindwurm());
        harness.setHand(player1, List.of(new DemonBolt()));
        addDemonBoltMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Ravenous Lindwurm");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DemonBolt()));
        addDemonBoltMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastOnTurnItWasForetold() {
        DemonBolt spell = new DemonBolt();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(),
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Demon Bolt");
    }

    @Test
    void foretellRequiresTwoMana() {
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Demon Bolt");
    }

    @Test
    void canCastForetoldInstantDuringOpponentsLaterTurn() {
        DemonBolt spell = new DemonBolt();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, spell.getId(), creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.assertInGraveyard(player1, "Demon Bolt");
    }

    @Test
    void doesNotDamageAnotherCreatureWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(new DemonBolt()));
        addDemonBoltMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Demon Bolt");
        harness.assertInHand(player2, "Ravenous Lindwurm");
    }

    private void addDemonBoltMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
