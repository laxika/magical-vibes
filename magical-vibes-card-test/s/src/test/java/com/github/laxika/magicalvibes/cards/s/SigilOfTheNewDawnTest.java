package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomedNecromancer;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilOfTheNewDawn.class, GlorySeeker.class, DoomedNecromancer.class})
class SigilOfTheNewDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} returns the creature to its owner's hand")
    void payingReturnsCreatureToHand() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        destroy(glorySeeker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Glory Seeker");
        harness.assertNotInGraveyard(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Declining leaves the creature in its owner's graveyard")
    void decliningLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        destroy(glorySeeker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Accepting without enough mana leaves the creature in its owner's graveyard")
    void acceptingWithoutEnoughManaLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        destroy(glorySeeker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Triggers for a creature you own even if an opponent controls it")
    void triggersForOwnedCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        gd.playerBattlefields.get(player1.getId()).remove(glorySeeker);
        gd.playerBattlefields.get(player2.getId()).add(glorySeeker);
        gd.stolenCreatures.put(glorySeeker.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        destroy(glorySeeker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Does not trigger for a creature owned by an opponent")
    void doesNotTriggerForOpponentOwnedCreature() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        gd.playerBattlefields.get(player2.getId()).remove(glorySeeker);
        gd.playerBattlefields.get(player1.getId()).add(glorySeeker);
        gd.stolenCreatures.put(glorySeeker.getId(), player2.getId());

        destroy(glorySeeker);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.assertNotInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the white part of the cost")
    void cannotPayWithoutWhiteMana() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        destroy(glorySeeker);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotInHand(player1, "Glory Seeker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger resolves after Sigil leaves the battlefield")
    void triggerSurvivesRemovalOfSigil() {
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfTheNewDawn());
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, glorySeeker);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sigil);
        });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Glory Seeker");
        harness.assertNotInGraveyard(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Sigil of the New Dawn");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An old trigger cannot return a creature that was reanimated and died again")
    void originalTriggerCannotReturnNewGraveyardObject() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        GlorySeeker card = new GlorySeeker();
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, card);
        addCreatureReady(player1, new DoomedNecromancer());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, glorySeeker));
        harness.activateAbilityWithGraveyardTargets(player1, 1, 0, List.of(card.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertNotInGraveyard(player1, "Glory Seeker");

        Permanent returnedCreature = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Glory Seeker"));
        destroy(returnedCreature);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotInHand(player1, "Glory Seeker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
