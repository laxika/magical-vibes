package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntropicSpecter.class, RagingGoblin.class})
class EntropicSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the opponent's hand size")
    void powerAndToughnessEqualOpponentHandSize() {
        harness.setHand(player1, handOf(5));
        harness.setHand(player2, handOf(3));
        Permanent specter = addSpecter(player1);

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update as the opponent's hand changes")
    void powerAndToughnessUpdateWithOpponentHand() {
        harness.setHand(player2, handOf(2));
        Permanent specter = addSpecter(player1);

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(2);

        harness.setHand(player2, handOf(3));

        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage to a player makes that player discard a card")
    void damageToPlayerTriggersDiscard() {
        harness.setHand(player2, handOf(2));
        Permanent specter = addSpecter(player1);
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage to a player makes that player discard a card")
    void noncombatDamageToPlayerTriggersDiscard() {
        harness.setHand(player2, List.of(new RagingGoblin()));
        EntropicSpecter card = new EntropicSpecter();
        card.addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."));
        Permanent specter = addCreatureReady(player1, card);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(specter), null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private Permanent addSpecter(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new EntropicSpecter());
    }

    private List<Card> handOf(int count) {
        return new ArrayList<>(java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> (Card) new RagingGoblin())
                .toList());
    }
}
