package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VarolzTheScarStripedTest extends BaseCardTest {

    private Permanent addVarolz() {
        return addCreatureReady(player1, new VarolzTheScarStriped());
    }

    @Test
    @DisplayName("Grants scavenge for the card's own mana cost to a creature card in your graveyard")
    void grantsScavengeEqualToManaCost() {
        addVarolz();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        // Grizzly Bears costs {1}{G}, so scavenge costs {1}{G}
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        // Grizzly Bears' power is 2
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Granted scavenge cannot be activated without enough mana for the card's mana cost")
    void grantedScavengeNeedsTheFullManaCost() {
        addVarolz();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant scavenge to a noncreature card in your graveyard")
    void doesNotGrantScavengeToNoncreatureCard() {
        addVarolz();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without Varolz on the battlefield, a creature card in the graveyard has no scavenge")
    void noScavengeWithoutVarolz() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing another creature grants Varolz a regeneration shield")
    void sacrificeGrantsRegenerationShield() {
        Permanent varolz = addVarolz();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(varolz.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Varolz itself cannot be sacrificed to its own ability")
    void cannotSacrificeItself() {
        Permanent varolz = addVarolz();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Varolz, the Scar-Striped");
        assertThat(varolz.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("The regeneration shield saves Varolz from lethal combat damage")
    void regenerationSavesFromLethalDamage() {
        Permanent varolz = addVarolz();
        varolz.setRegenerationShield(1);
        varolz.setBlocking(true);
        varolz.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Varolz, the Scar-Striped");
        assertThat(varolz.isTapped()).isTrue();
        assertThat(varolz.getRegenerationShield()).isEqualTo(0);
    }
}
