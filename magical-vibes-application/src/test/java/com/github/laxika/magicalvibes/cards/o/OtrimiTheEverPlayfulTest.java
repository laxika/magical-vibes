package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BoneyardLurker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OtrimiTheEverPlayful.class, BoneyardLurker.class, GrizzlyBears.class, Opt.class})
class OtrimiTheEverPlayfulTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a target creature card with mutate from its controller's graveyard")
    void combatDamageReturnsCreatureCardWithMutate() {
        Card eligible = new BoneyardLurker();
        Card ordinaryCreature = new GrizzlyBears();
        Card noncreature = new Opt();
        harness.setGraveyard(player1, List.of(eligible, ordinaryCreature, noncreature));

        attackWithOtrimiDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(ordinaryCreature, noncreature);
    }

    @Test
    @DisplayName("The trigger does not target a creature with no mutate ability")
    void ordinaryCreatureIsNotAValidTarget() {
        Card ordinaryCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ordinaryCreature));

        attackWithOtrimiDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ordinaryCreature);
    }

    @Test
    @DisplayName("The trigger only targets a card in the controller's graveyard")
    void opponentGraveyardIsNotAValidTarget() {
        Card opponentMutateCreature = new BoneyardLurker();
        harness.setGraveyard(player2, List.of(opponentMutateCreature));

        attackWithOtrimiDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentMutateCreature);
    }

    @Test
    @DisplayName("Mutate costs one generic, one black, one green and one blue mana and merges with its target")
    void mutateCostMergesWithOwnedNonHuman() {
        Permanent host = addCreatureReady(player1, new OtrimiTheEverPlayful());
        harness.setHand(player1, List.of(new OtrimiTheEverPlayful()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getId()).isEqualTo(host.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A graveyard target that leaves before resolution is not returned")
    void missingTargetIsNotReturned() {
        Card eligible = new OtrimiTheEverPlayful();
        harness.setGraveyard(player1, List.of(eligible));
        attackWithOtrimiDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(eligible);
        gd.exiledCards.add(new ExiledCardEntry(eligible, player1.getId(), null));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligible);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(eligible.getId()));
    }

    @Test
    @DisplayName("The graveyard return resolves after Otrimi leaves the battlefield")
    void returnResolvesWithoutSource() {
        Card eligible = new OtrimiTheEverPlayful();
        harness.setGraveyard(player1, List.of(eligible));
        attackWithOtrimiDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(eligible);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source.getCard());
    }

    private void attackWithOtrimiDealingDamage() {
        Permanent otrimi = addCreatureReady(player1, new OtrimiTheEverPlayful());
        otrimi.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
