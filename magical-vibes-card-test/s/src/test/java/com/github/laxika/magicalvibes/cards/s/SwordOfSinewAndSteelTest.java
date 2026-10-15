package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfSinewAndSteel.class, GrizzlyBears.class, ChandraNalaar.class, MindStone.class, LiquimetalCoating.class})
class SwordOfSinewAndSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from black and red")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage trigger destroys up to one planeswalker and up to one artifact")
    void combatDamageDestroysPlaneswalkerAndArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(planeswalker, artifact);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(planeswalker.getCard(), artifact.getCard());
    }

    @Test
    @DisplayName("Equip attaches the Sword for two mana and grants its bonuses")
    void equipAttachesSword() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Both targets may be declined even when legal targets exist")
    void mayDeclineBothTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker, artifact);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the planeswalker target still destroys the chosen artifact")
    void mayDestroyOnlyArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Declining the artifact target still destroys the chosen planeswalker")
    void mayDestroyOnlyPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        resolveCombat();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact).doesNotContain(planeswalker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(planeswalker.getCard());
    }

    @Test
    @DisplayName("The Sword may destroy itself without undoing combat damage")
    void mayTargetSwordItselfWithoutAnyPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, sword.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(sword);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sword.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("A planeswalker leaving before resolution does not save the artifact")
    void remainingLegalTargetIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent planeswalker = addPlaneswalker(player2, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        resolveCombat();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("An artifact planeswalker may be chosen for both target clauses")
    void mayChooseSameArtifactPlaneswalkerTwice() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent planeswalker = addPlaneswalker(player2, 5);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, planeswalker.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice artifactChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(artifactChoice.validPermanentIds()).contains(planeswalker.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(planeswalker.getCard());
    }

    private Permanent addSwordReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SwordOfSinewAndSteel());
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
