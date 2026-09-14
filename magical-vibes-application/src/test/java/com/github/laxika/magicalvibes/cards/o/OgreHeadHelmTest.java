package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreHeadHelm.class, GrizzlyBears.class})
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
}
