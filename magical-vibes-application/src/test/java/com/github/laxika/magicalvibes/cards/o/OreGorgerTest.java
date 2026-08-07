package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OreGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell lets the controller destroy the targeted nonbasic land")
    void arcaneCastDestroysNonbasicLand() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID landId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Declining the may leaves the land on the battlefield")
    void decliningLeavesLandAlive() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID landId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Casting a Spirit spell triggers the ability")
    void spiritCastTriggers() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID landId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.setHand(player1, List.of(new OreGorger()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("A basic land is not a legal target")
    void basicLandIsNotALegalTarget() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }
}
