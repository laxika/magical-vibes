package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Bullwhip;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SoldeviGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArgothianTreefolk.class, Bullwhip.class, BalduvianBears.class,
        ProdigalSorcerer.class, SoldeviGolem.class})
class ArgothianTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage from an artifact source")
    void preventsArtifactAbilityDamage() {
        harness.addToBattlefield(player1, new Bullwhip());
        Permanent treefolk = addCreatureReady(player2, new ArgothianTreefolk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from a nonartifact source")
    void allowsNonartifactAbilityDamage() {
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent treefolk = addCreatureReady(player2, new ArgothianTreefolk());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer), null,
                treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage from an artifact source")
    void preventsArtifactCombatDamage() {
        Permanent treefolk = addCreatureReady(player2, new ArgothianTreefolk());
        Permanent attacker = addCreatureReady(player1, new SoldeviGolem());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(treefolk),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage from a nonartifact source")
    void allowsNonartifactCombatDamage() {
        Permanent treefolk = addCreatureReady(player2, new ArgothianTreefolk());
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(treefolk),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isEqualTo(2);
    }
}
