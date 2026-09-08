package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SparkOfCreativityTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the exiled card's mana value")
    void dealsDamageEqualToExiledCardManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        castSpark(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Grants play permission when the damage is declined")
    void grantsPlayPermissionWhenDamageIsDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        castSpark(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void doesNothingWhenLibraryIsEmpty() {
        gd.playerDecks.get(player1.getId()).clear();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castSpark(target);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    private void castSpark(Permanent target) {
        harness.setHand(player1, List.of(new SparkOfCreativity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
