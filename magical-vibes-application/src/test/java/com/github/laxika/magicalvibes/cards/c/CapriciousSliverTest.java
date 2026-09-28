package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CapriciousSliver.class, SinewSliver.class, GrizzlyBears.class})
class CapriciousSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver's combat damage exiles the top card of its controller's library for play")
    void sliverCombatDamageExilesTopCardForPlay() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addAttackingCreature(player1, new CapriciousSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("The ability is granted to other Slivers you control")
    void otherSliverAlsoExilesTopCardForPlay() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new CapriciousSliver());
        addAttackingCreature(player1, new SinewSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A non-Sliver does not gain the ability")
    void nonSliverDoesNotExileTopCard() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new CapriciousSliver());
        addAttackingCreature(player1, new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Each Sliver that deals combat damage exiles a separate top card")
    void eachSliverTriggersSeparately() {
        Card firstTopCard = new GrizzlyBears();
        Card secondTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard));
        addAttackingCreature(player1, new SinewSliver());
        addAttackingCreature(player1, new CapriciousSliver());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstTopCard.getId(), secondTopCard.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(firstTopCard.getId(), player1.getId())
                .containsEntry(secondTopCard.getId(), player1.getId());
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
