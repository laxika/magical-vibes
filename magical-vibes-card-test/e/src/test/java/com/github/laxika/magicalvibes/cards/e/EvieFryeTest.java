package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JacobFrye;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvieFrye.class, Forest.class, JacobFrye.class})
class EvieFryeTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the targeted player search for Jacob Frye")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card jacob = new JacobFrye();
        harness.setLibrary(player2, List.of(jacob));
        harness.setHand(player1, List.of(new EvieFrye()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(jacob);
    }

    @Test
    @DisplayName("Discarding a creature lets the controller choose one of their creatures to unblock")
    void creatureDiscardMakesOwnCreatureUnblockable() {
        addCreatureReady(player1, new EvieFrye());
        Permanent ownCreature = addCreatureReady(player1, new JacobFrye());
        Permanent opposingCreature = addCreatureReady(player2, new JacobFrye());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new JacobFrye()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBeBlocked()).isTrue();
        assertThat(opposingCreature.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Discarding a noncreature does not create the unblockable trigger")
    void nonCreatureDiscardDoesNotTrigger() {
        addCreatureReady(player1, new EvieFrye());
        Permanent ownCreature = addCreatureReady(player1, new JacobFrye());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The targeted player may decline the partner search")
    void partnerSearchCanBeDeclined() {
        Card jacob = new JacobFrye();
        harness.setLibrary(player2, List.of(jacob));
        harness.setHand(player1, List.of(new EvieFrye()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(jacob);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(jacob);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Partner search does not find a differently named card")
    void partnerSearchWithNoJacob() {
        Card forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.setHand(player1, List.of(new EvieFrye()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Evie can target herself only after the creature is discarded")
    void creatureDiscardCreatesSeparateSelfTargetableTrigger() {
        Permanent evie = addCreatureReady(player1, new EvieFrye());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card discarded = new JacobFrye();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(evie.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(evie.getId()).doesNotContain(forest.getId());
        harness.handlePermanentChosen(player1, evie.getId());
        assertThat(evie.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(evie.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(evie.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The activated ability still resolves after Evie leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent evie = addCreatureReady(player1, new EvieFrye());
        Permanent jacob = addCreatureReady(player1, new JacobFrye());
        Card discarded = new JacobFrye();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(evie);
        harness.setGraveyard(player1, List.of(evie.getCard()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, jacob.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(jacob.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The reflexive trigger does not affect a creature that changes controllers")
    void targetMustStillBeControlledOnResolution() {
        addCreatureReady(player1, new EvieFrye());
        Permanent jacob = addCreatureReady(player1, new JacobFrye());
        harness.setHand(player1, List.of(new JacobFrye()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, jacob.getId());
        gd.playerBattlefields.get(player1.getId()).remove(jacob);
        gd.playerBattlefields.get(player2.getId()).add(jacob);
        harness.passBothPriorities();

        assertThat(jacob.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
