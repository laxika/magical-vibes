package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AthreosGodOfPassage.class, GrizzlyBears.class, WalkingCorpse.class})
class AthreosGodOfPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Athreos is not a creature below seven devotion to white and black")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    @DisplayName("Athreos becomes a creature at seven devotion to white and black")
    void becomesCreatureAtDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(5);

        assertThat(gqs.isCreature(gd, athreos)).isTrue();
    }

    @Test
    @DisplayName("The target opponent may pay 3 life to keep an owned dead creature in the graveyard")
    void targetOpponentMayPayLife() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The dead creature returns to its owner's hand when the opponent declines")
    void decliningPaymentReturnsCreatureToOwnerHand() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Athreos does not trigger for a creature owned by an opponent")
    void doesNotTriggerForOpponentOwnedCreature() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroy(bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent below three life cannot pay and the creature returns")
    void insufficientLifeReturnsCreature() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        harness.setLife(player2, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An owned creature controlled by an opponent returns to its owner")
    void ownedCreatureUnderOpponentControlReturnsToOwner() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player1.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, card);

        destroy(bears);
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Athreos does not trigger for its own death")
    void doesNotTriggerForItsOwnDeath() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(5);
        athreos.setToughnessModifier(-4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Athreos, God of Passage");
        harness.assertNotInHand(player1, "Athreos, God of Passage");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Athreos triggers for another owned creature dying at the same time")
    void simultaneousDeathStillReturnsOtherCreature() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(5);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        athreos.setToughnessModifier(-4);
        bears.setToughnessModifier(-2);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Athreos, God of Passage");
        harness.assertNotInHand(player1, "Athreos, God of Passage");
    }

    @Test
    @DisplayName("Athreos stops being a creature when devotion drops below seven")
    void losesCreatureTypeWhenDevotionDrops() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(5);
        Permanent corpse = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.isCreature(gd, athreos)).isTrue();

        destroy(corpse);
        assertThat(gqs.isCreature(gd, athreos)).isFalse();
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Athreos, God of Passage");
        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    @DisplayName("Athreos cannot be destroyed while it is a creature")
    void indestructiblePreventsDestruction() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(5);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, athreos));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Athreos, God of Passage");
        harness.assertNotInGraveyard(player1, "Athreos, God of Passage");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void chooseOpponentTarget() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }
}
