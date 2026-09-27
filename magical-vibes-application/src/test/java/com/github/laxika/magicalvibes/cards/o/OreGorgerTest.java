package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForbiddenOrchard;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OreGorger.class, DampenThought.class, ForbiddenOrchard.class, Forest.class,
        HumbleBudoka.class})
class OreGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell lets the controller destroy the targeted nonbasic land")
    void arcaneCastDestroysNonbasicLand() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        UUID landId = harness.getPermanentId(player2, "Forbidden Orchard");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Forbidden Orchard");
        harness.assertInGraveyard(player2, "Forbidden Orchard");
    }

    @Test
    @DisplayName("Declining the may leaves the land on the battlefield")
    void decliningLeavesLandAlive() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        UUID landId = harness.getPermanentId(player2, "Forbidden Orchard");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Forbidden Orchard");
    }

    @Test
    @DisplayName("Casting a Spirit spell triggers the ability")
    void spiritCastTriggers() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        UUID landId = harness.getPermanentId(player2, "Forbidden Orchard");
        harness.castFromHand(player1, new OreGorger(), "{3}{R}{R}");
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Forbidden Orchard");
    }

    @Test
    @DisplayName("A basic land is not a legal target")
    void basicLandIsNotALegalTarget() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new ForbiddenOrchard());
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
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        harness.castFromHand(player1, new HumbleBudoka(), "{1}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Spirit or Arcane spell does not trigger the ability")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.setHand(player2, List.of(new DampenThought()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A nonland permanent is not a legal target")
    void nonlandPermanentIsNotALegalTarget() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player2, new ForbiddenOrchard());
        harness.addToBattlefield(player2, new HumbleBudoka());
        UUID creatureId = harness.getPermanentId(player2, "Humble Budoka");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller may target their own nonbasic land")
    void ownNonbasicLandIsALegalTarget() {
        harness.addToBattlefield(player1, new OreGorger());
        harness.addToBattlefield(player1, new ForbiddenOrchard());
        UUID landId = harness.getPermanentId(player1, "Forbidden Orchard");
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, landId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Forbidden Orchard");
    }
}
