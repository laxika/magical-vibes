package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JestersMask;
import com.github.laxika.magicalvibes.cards.k.KhalniGarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmuletOfVigor.class, JestersMask.class, KhalniGarden.class})
class AmuletOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a permanent you control that enters tapped")
    void untapsOwnPermanentThatEntersTapped() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.setHand(player1, List.of(new JestersMask()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mask = findPermanent(player1, "Jester's Mask");
        assertThat(mask.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap an opponent's permanent")
    void doesNotUntapOpponentsPermanent() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new JestersMask()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent mask = findPermanent(player2, "Jester's Mask");
        assertThat(mask.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped land stays tapped until the Amulet trigger resolves")
    void untapsLandOnResolution() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.setHand(player1, List.of(new KhalniGarden()));

        harness.playLand(player1, 0);

        Permanent garden = findPermanent(player1, "Khalni Garden");
        assertThat(garden.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(garden.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Plant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped permanent entering does not trigger Amulet")
    void doesNotTriggerForUntappedEntry() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.setHand(player1, List.of(new AmuletOfVigor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Amulet of Vigor")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Amulet independently triggers for the same tapped permanent")
    void multipleAmuletsTriggerIndependently() {
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.addToBattlefield(player1, new AmuletOfVigor());
        harness.setHand(player1, List.of(new JestersMask()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent mask = findPermanent(player1, "Jester's Mask");
        assertThat(mask.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(mask.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(mask.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
