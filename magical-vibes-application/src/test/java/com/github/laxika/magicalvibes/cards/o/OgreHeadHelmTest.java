package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreHeadHelm.class, GrizzlyBears.class, EnchantedEvening.class})
class OgreHeadHelmTest extends BaseCardTest {

    @Test
    void helmGetsSacrificedAfterItsCombatDamageAndDrawsThree() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        helm.setAttacking(true);
        GrizzlyBears discardedCard = new GrizzlyBears();
        GrizzlyBears drawnCardOne = new GrizzlyBears();
        GrizzlyBears drawnCardTwo = new GrizzlyBears();
        GrizzlyBears drawnCardThree = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCardOne, drawnCardTwo, drawnCardThree));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(helm);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard, helm.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(
                drawnCardOne, drawnCardTwo, drawnCardThree);
    }

    @Test
    void equippedCreatureGetsSacrificedAfterItsCombatDamage() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        helm.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        GrizzlyBears discardedCard = new GrizzlyBears();
        GrizzlyBears drawnCardOne = new GrizzlyBears();
        GrizzlyBears drawnCardTwo = new GrizzlyBears();
        GrizzlyBears drawnCardThree = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCardOne, drawnCardTwo, drawnCardThree));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(helm);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard, creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(
                drawnCardOne, drawnCardTwo, drawnCardThree);
    }

    @Test
    void decliningSacrificeLeavesTheDamagingPermanentOnTheBattlefield() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        helm.setAttacking(true);
        GrizzlyBears handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(helm);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheHelm() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    void sacrificeDrawsThreeEvenWhenHandIsEmpty() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        helm.setAttacking(true);
        harness.setHand(player1, List.of());
        OgreHeadHelm first = new OgreHeadHelm();
        OgreHeadHelm second = new OgreHeadHelm();
        OgreHeadHelm third = new OgreHeadHelm();
        harness.setLibrary(player1, List.of(first, second, third));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(helm.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void cannotSacrificeAnEquippedCreatureControlledByTheOpponent() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        helm.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        OgreHeadHelm handCard = new OgreHeadHelm();
        harness.setHand(player1, List.of(handCard));

        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(helm);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
    }

    @Test
    void reconfigureRemovesCreatureTypeAndUnattachingRestoresIt() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        Permanent creature = addCreatureReady(player1, new OgreHeadHelm());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, helm)).isFalse();
        assertThat(gqs.isCreature(gd, creature)).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, helm)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void bothReconfigureModesRequireSorceryTiming() {
        Permanent helm = addCreatureReady(player1, new OgreHeadHelm());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        helm.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigurePreservesOtherCardTypes() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new OgreHeadHelm());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OgreHeadHelm());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThat(gqs.isEnchantment(gd, helm)).isTrue();

        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, helm)).isFalse();
        assertThat(gqs.isArtifact(gd, helm)).isTrue();
        assertThat(gqs.isEnchantment(gd, helm)).isTrue();
    }
}
