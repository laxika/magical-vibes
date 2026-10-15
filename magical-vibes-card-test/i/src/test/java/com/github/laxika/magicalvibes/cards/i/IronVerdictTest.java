package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
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

@CardUsed({IronVerdict.class, GrizzlyBears.class, GoldveinPick.class, RavenousLindwurm.class})
class IronVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a tapped creature")
    void dealsDamageToTappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Can be foretold and cast for white mana on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();
        IronVerdict verdict = new IronVerdict();
        harness.setHand(player1, List.of(verdict));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(verdict.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, verdict.getId(), bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marks exactly 5 damage on a tapped creature that survives")
    void marksFiveDamageOnSurvivingCreature() {
        Permanent wurm = addCreatureReady(player2, new RavenousLindwurm());
        wurm.tap();
        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, wurm.getId());

        harness.assertOnBattlefield(player2, "Ravenous Lindwurm");
        assertThat(wurm.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Iron Verdict");
    }

    @Test
    @DisplayName("Can target a tapped creature controlled by its caster")
    void canTargetOwnTappedCreature() {
        Permanent wurm = addCreatureReady(player1, new RavenousLindwurm());
        wurm.tap();
        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, wurm.getId());

        assertThat(wurm.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Ravenous Lindwurm");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreature() {
        Permanent pick = new Permanent(new GoldveinPick());
        pick.tap();
        gd.playerBattlefields.get(player2.getId()).add(pick);
        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, pick.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Iron Verdict");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not deal damage if the target untaps before resolution")
    void targetUntapsBeforeResolution() {
        Permanent wurm = addCreatureReady(player2, new RavenousLindwurm());
        wurm.tap();
        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, wurm.getId());

        wurm.untap();
        harness.passBothPriorities();

        assertThat(wurm.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Ravenous Lindwurm");
        harness.assertInGraveyard(player1, "Iron Verdict");
    }

    @Test
    @DisplayName("Cannot cast a foretold Iron Verdict during the turn it was foretold")
    void cannotCastOnTurnItWasForetold() {
        Permanent wurm = addCreatureReady(player2, new RavenousLindwurm());
        wurm.tap();
        IronVerdict verdict = new IronVerdict();
        harness.setHand(player1, List.of(verdict));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, verdict.getId(), wurm.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(verdict.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Foretelling requires two mana")
    void cannotForetellWithOnlyOneMana() {
        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough mana to foretell");
        harness.assertInHand(player1, "Iron Verdict");
    }

    @Test
    @DisplayName("Cannot foretell during an opponent's turn")
    void cannotForetellDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new IronVerdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");
        harness.assertInHand(player1, "Iron Verdict");
    }

    @Test
    @DisplayName("Can cast foretold Iron Verdict during an opponent's later turn")
    void canCastDuringOpponentsLaterTurn() {
        Permanent wurm = addCreatureReady(player2, new RavenousLindwurm());
        wurm.tap();
        IronVerdict verdict = new IronVerdict();
        harness.setHand(player1, List.of(verdict));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInHand(player1, "Iron Verdict");

        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, verdict.getId(), wurm.getId());
        harness.passBothPriorities();

        assertThat(wurm.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.findExiledCard(verdict.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Iron Verdict");
    }
}
