package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EpicStruggle;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanReclamation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForcemageAdvocate.class, EpicStruggle.class, GiantWarthog.class, KrosanReclamation.class})
class ForcemageAdvocateTest extends BaseCardTest {

    @Test
    void returnsOpponentGraveyardCardAndPutsCounterOnTargetCreature() {
        Permanent advocate = addReadyAdvocate();
        Permanent creature = addCreatureReady(player1, new GiantWarthog());
        Card returnedCard = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(returnedCard));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(returnedCard.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(returnedCard.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    void rejectsOwnGraveyardCardAsTheFirstTarget() {
        Permanent advocate = addReadyAdvocate();
        Permanent creature = addCreatureReady(player1, new GiantWarthog());
        Card ownCard = new KrosanReclamation();
        harness.setGraveyard(player1, List.of(ownCard));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(ownCard.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonCreatureAsTheSecondTarget() {
        Permanent advocate = addReadyAdvocate();
        Permanent nonCreature = new Permanent(new EpicStruggle());
        gd.playerBattlefields.get(player1.getId()).add(nonCreature);
        Card returnedCard = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(returnedCard));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(returnedCard.getId(), nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exposesOpponentGraveyardAndCreatureAsSeparateTargetGroups() {
        Permanent advocate = addReadyAdvocate();
        Permanent creature = addCreatureReady(player1, new GiantWarthog());
        Card returnedCard = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(returnedCard));

        var ability = advocate.getCard().getActivatedAbilities().getFirst();
        ValidTargetsResponse firstTargets = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, advocate.getCard(), ability, player1.getId(), index(advocate));
        assertThat(firstTargets.validGraveyardCardIds()).containsExactly(returnedCard.getId());

        ValidTargetsResponse creatureTargets = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, advocate.getCard(), ability, player1.getId(), index(advocate),
                List.of(returnedCard.getId()));
        assertThat(creatureTargets.validPermanentIds()).contains(creature.getId());
    }

    private Permanent addReadyAdvocate() {
        return addCreatureReady(player1, new ForcemageAdvocate());
    }

    private int index(Permanent advocate) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(advocate);
    }
}
