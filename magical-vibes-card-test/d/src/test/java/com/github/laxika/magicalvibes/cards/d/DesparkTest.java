package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GodPharaohsStatue;
import com.github.laxika.magicalvibes.cards.k.KarnTheGreatCreator;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Despark.class, HillGiant.class, GrizzlyBears.class, Forest.class,
        GodPharaohsStatue.class, KarnTheGreatCreator.class})
class DesparkTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target permanent with mana value 4 or greater")
    void exilesTargetPermanentWithManaValueAtLeastFour() {
        harness.addToBattlefield(player2, new HillGiant());
        castDespark(harness.getPermanentId(player2, "Hill Giant"));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value less than 4")
    void cannotTargetPermanentWithManaValueLessThanFour() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> castDespark(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> castDespark(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles your own noncreature artifact with mana value greater than four")
    void exilesOwnNoncreatureArtifact() {
        harness.addToBattlefield(player1, new GodPharaohsStatue());

        castDespark(harness.getPermanentId(player1, "God-Pharaoh's Statue"));

        harness.assertNotOnBattlefield(player1, "God-Pharaoh's Statue");
        harness.assertNotInGraveyard(player1, "God-Pharaoh's Statue");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("God-Pharaoh's Statue"));
    }

    @Test
    @DisplayName("Exiles an opposing planeswalker with mana value exactly four")
    void exilesPlaneswalker() {
        Permanent karn = harness.addToBattlefieldAndReturn(player2, new KarnTheGreatCreator());
        karn.setCounterCount(CounterType.LOYALTY, 5);

        castDespark(karn.getId());

        harness.assertNotOnBattlefield(player2, "Karn, the Great Creator");
        harness.assertNotInGraveyard(player2, "Karn, the Great Creator");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Karn, the Great Creator"));
    }

    @Test
    @DisplayName("Does not exile a target again after another Despark removes it")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GodPharaohsStatue());
        UUID targetId = harness.getPermanentId(player1, "God-Pharaoh's Statue");
        harness.setHand(player1, List.of(new Despark(), new Despark()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "God-Pharaoh's Statue");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("God-Pharaoh's Statue"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Despark"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void castDespark(UUID targetId) {
        harness.setHand(player1, List.of(new Despark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
