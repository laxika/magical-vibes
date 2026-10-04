package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimBounty.class, GrizzlyBears.class, GarrukWildspeaker.class, Plains.class})
class GrimBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates a Treasure")
    void destroysCreatureAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Destroys a planeswalker and creates a Treasure")
    void destroysPlaneswalkerAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither a creature nor a planeswalker")
    void cannotTargetNonCreatureNonPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new GrimBounty()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Creates no Treasure when the only target leaves before resolution")
    void noTreasureWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrimBounty()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grim Bounty");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates a Treasure even when the legal target is indestructible")
    void createsTreasureWhenDestructionIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can destroy your own creature and still creates a Treasure")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new GrimBounty()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
