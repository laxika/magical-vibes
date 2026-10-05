package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Anticipate;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoreDrakkis.class, Opt.class, Divination.class, GrizzlyBears.class, Anticipate.class})
class LoreDrakkisTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating returns a chosen instant or sorcery from your graveyard to your hand")
    void mutatingReturnsChosenSpellToHand() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card opt = new Opt();
        Card divination = new Divination();
        harness.setGraveyard(player1, List.of(opt, divination));

        triggerMutation(drakkis);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opt.getId(), divination.getId());

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("Mutating cannot target a non-instant or non-sorcery card")
    void mutatingCannotTargetPermanentCard() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        triggerMutation(drakkis);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mutating cannot target an instant or sorcery in an opponent's graveyard")
    void mutatingCannotTargetOpponentGraveyard() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card opt = new Opt();
        harness.setGraveyard(player2, List.of(opt));

        triggerMutation(drakkis);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Opt");
    }

    @Test
    @DisplayName("Casting normally does not return a spell from the graveyard")
    void normalCastDoesNotReturnSpell() {
        Card spell = new Anticipate();
        harness.setGraveyard(player1, List.of(spell));

        harness.castFromHand(player1, new LoreDrakkis(), "{1}{U}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lore Drakkis");
        harness.assertInGraveyard(player1, "Anticipate");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lore Drakkis can be cast for its mutate cost using two blue mana")
    void canCastForMutateCost() {
        Permanent target = addCreatureReady(player1, new LoreDrakkis());
        harness.setHand(player1, List.of(new LoreDrakkis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A mutation with a legal graveyard target cannot be declined")
    void mandatoryTargetCannotBeDeclined() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card spell = new Anticipate();
        harness.setGraveyard(player1, List.of(spell));
        triggerMutation(drakkis);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        resolveAllTriggers();
        harness.assertInHand(player1, "Anticipate");
    }

    @Test
    @DisplayName("A graveyard target removed before resolution is not returned or replaced")
    void removedTargetDoesNotReturnAnotherSpell() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card target = new Anticipate();
        Card other = new Anticipate();
        harness.setGraveyard(player1, List.of(target, other));
        triggerMutation(drakkis);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target, other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The mutation trigger resolves after Lore Drakkis leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card spell = new Anticipate();
        harness.setGraveyard(player1, List.of(spell));
        triggerMutation(drakkis);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(drakkis);
            gd.playerGraveyards.get(player1.getId()).add(drakkis.getCard());
        });
        resolveAllTriggers();

        harness.assertInHand(player1, "Anticipate");
        harness.assertInGraveyard(player1, "Lore Drakkis");
    }
    private void triggerMutation(Permanent drakkis) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, drakkis, List.of(drakkis.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
