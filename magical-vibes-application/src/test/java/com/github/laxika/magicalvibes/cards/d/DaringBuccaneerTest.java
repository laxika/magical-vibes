package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaringBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Pirate in hand it requires the additional {2}")
    void requiresAdditionalManaWithoutPirate() {
        harness.setHand(player1, List.of(new DaringBuccaneer()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {2} can be paid when no Pirate is revealed")
    void paysAdditionalManaWithoutPirate() {
        DaringBuccaneer buccaneer = new DaringBuccaneer();
        harness.setHand(player1, List.of(buccaneer));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(buccaneer.getId()));
    }

    @Test
    @DisplayName("A Pirate in hand lets it be cast without paying the additional {2}")
    void revealPirateAvoidsAdditionalMana() {
        DaringBuccaneer buccaneer = new DaringBuccaneer();
        SailorOfMeans pirateInHand = new SailorOfMeans();
        harness.setHand(player1, List.of(buccaneer, pirateInHand));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(buccaneer.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(pirateInHand.getId()));
    }
}
