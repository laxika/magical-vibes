package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PulsemageAdvocate.class, GrizzlyBears.class, LightningBolt.class, Shock.class, Twiddle.class})
class PulsemageAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Returns three opponent cards to their hand and reanimates your creature")
    void returnsOpponentCardsAndReanimatesOwnCreature() {
        Permanent advocate = addReadyAdvocate();
        Card first = new LightningBolt();
        Card second = new Shock();
        Card third = new Twiddle();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbilityWithGraveyardTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), third.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a card from your graveyard in the opponent-card target group")
    void rejectsOwnCardForOpponentTargetGroup() {
        Permanent advocate = addReadyAdvocate();
        Card firstOpponentCard = new LightningBolt();
        Card secondOpponentCard = new Shock();
        Card thirdOpponentCard = new Twiddle();
        Card ownCard = new Shock();
        Card ownCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstOpponentCard, secondOpponentCard, thirdOpponentCard));
        harness.setGraveyard(player1, List.of(ownCard, ownCreature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index(advocate), 0,
                List.of(firstOpponentCard.getId(), ownCard.getId(), thirdOpponentCard.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Offers opponent graveyard cards for the first three targets and own creatures for the fourth")
    void exposesSeparateTargetGroups() {
        Permanent advocate = addReadyAdvocate();
        Card first = new LightningBolt();
        Card second = new Shock();
        Card third = new Twiddle();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(creature));

        var ability = advocate.getCard().getActivatedAbilities().getFirst();
        ValidTargetsResponse firstTargets = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, advocate.getCard(), ability, player1.getId(), index(advocate));
        assertThat(firstTargets.validGraveyardCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());

        ValidTargetsResponse creatureTargets = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, advocate.getCard(), ability, player1.getId(), index(advocate),
                List.of(first.getId(), second.getId(), third.getId()));
        assertThat(creatureTargets.validGraveyardCardIds()).containsExactly(creature.getId());
    }

    private Permanent addReadyAdvocate() {
        Permanent advocate = new Permanent(new PulsemageAdvocate());
        advocate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(advocate);
        return advocate;
    }

    private int index(Permanent advocate) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(advocate);
    }
}
