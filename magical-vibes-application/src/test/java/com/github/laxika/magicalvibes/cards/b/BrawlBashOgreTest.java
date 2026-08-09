package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrawlBashOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature")
    void attackingOffersSacrifice() {
        Permanent ogre = addCreatureReady(player1, new BrawlBashOgre());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(ogre.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature gives Brawl-Bash Ogre +2/+2")
    void sacrificingAnotherCreatureBoosts() {
        Permanent ogre = addCreatureReady(player1, new BrawlBashOgre());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not boost Brawl-Bash Ogre")
    void decliningSacrificeDoesNothing() {
        Permanent ogre = addCreatureReady(player1, new BrawlBashOgre());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("With no other creature, accepting the may does nothing")
    void noOtherCreatureDoesNothing() {
        Permanent ogre = addCreatureReady(player1, new BrawlBashOgre());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
