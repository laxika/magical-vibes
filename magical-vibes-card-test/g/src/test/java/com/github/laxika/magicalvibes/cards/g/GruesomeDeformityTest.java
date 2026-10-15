package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WoodenStake;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.s.SilverchaseFox;
import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruesomeDeformity.class, WalkingCorpse.class, WoodenStake.class,
        SilverchaseFox.class, OneEyedScarecrow.class})
class GruesomeDeformityTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Gruesome Deformity")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new GruesomeDeformity()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Gruesome Deformity")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player1, new WoodenStake());
        harness.setHand(player1, List.of(new GruesomeDeformity()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Wooden Stake");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature has intimidate")
    void enchantedCreatureHasIntimidate() {
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GruesomeDeformity());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses intimidate when Gruesome Deformity is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GruesomeDeformity());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void resolvesAttachedToOpponentsCreatureAndOnlyGrantsItIntimidate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new GruesomeDeformity()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Gruesome Deformity");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void intimidateRejectsCreatureThatDoesNotShareAColor() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GruesomeDeformity());
        aura.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new SilverchaseFox());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void intimidateAllowsCreatureThatSharesAColor() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GruesomeDeformity());
        aura.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateAllowsArtifactCreatureWithoutSharedColor() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GruesomeDeformity());
        aura.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new OneEyedScarecrow());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
