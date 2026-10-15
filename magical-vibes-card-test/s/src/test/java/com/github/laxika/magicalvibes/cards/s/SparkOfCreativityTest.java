package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkOfCreativity.class, AirElemental.class, GrizzlyBears.class,
        ConsulateSkygate.class, Forest.class})
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

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class, Forest.class})
    void choosingZeroDamageDoesNotGrantPlayPermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castSpark(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class, Forest.class})
    void declinedDamageAllowsPlayingTheExiledLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castSpark(target);
        harness.handleMayAbilityChosen(player1, false);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class})
    void declinedDamageAllowsCastingTheExiledCardForItsNormalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new ConsulateSkygate();
        harness.setLibrary(player1, List.of(topCard));

        castSpark(target);
        harness.handleMayAbilityChosen(player1, false);
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Consulate Skygate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class})
    void playPermissionExpiresAtEndOfTurnWithoutMovingTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new ConsulateSkygate();
        harness.setLibrary(player1, List.of(topCard));

        castSpark(target);
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class})
    void illegalTargetPreventsExilingTheTopCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new ConsulateSkygate();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new SparkOfCreativity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Spark of Creativity");
    }

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class})
    void playPermissionDoesNotAllowCastingACreatureOutsideMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new ConsulateSkygate();
        harness.setLibrary(player1, List.of(topCard));

        castSpark(target);
        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @CardUsed({SparkOfCreativity.class, ConsulateSkygate.class, Forest.class})
    void playPermissionDoesNotGrantAnAdditionalLandPlay() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castSpark(target);
        harness.handleMayAbilityChosen(player1, false);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private void castSpark(Permanent target) {
        harness.setHand(player1, List.of(new SparkOfCreativity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
