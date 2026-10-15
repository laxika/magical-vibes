package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptedRoots.class, Forest.class, Plains.class, Mountain.class, ResoundingWave.class})
class CorruptedRootsTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Corrupted Roots targeting a Forest")
    void canTargetForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new CorruptedRoots()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Can cast Corrupted Roots targeting a Plains")
    void canTargetPlains() {
        harness.addToBattlefield(player1, new Plains());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new CorruptedRoots()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Cannot cast Corrupted Roots targeting a land that is neither Forest nor Plains")
    void cannotTargetOtherLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = findPermanent(player1, "Mountain");
        harness.setHand(player1, List.of(new CorruptedRoots()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Forest or Plains");
    }

    @Test
    @DisplayName("Tapping the enchanted land queues the trigger (deferred as a mana-ability trigger)")
    void tappingLandQueuesTrigger() {
        addLandWithAura(player1);

        // Tapping a land for mana defers its triggers until a player next gets priority.
        harness.tapPermanent(player1, 0);

        assertThat(gd.pendingManaAbilityTriggers).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Corrupted Roots");
        });
    }

    @Test
    @DisplayName("Tapping the enchanted land makes its controller lose 2 life")
    void tappingLandLosesLife() {
        addLandWithAura(player1);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Life loss hits the enchanted land's controller, not the Aura's controller")
    void losesLandControllerLifeNotAuraController() {
        // Aura is controlled by player1 but attached to a Forest player2 controls.
        harness.addToBattlefield(player2, new Forest());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();

        CorruptedRoots auraCard = new CorruptedRoots();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not cause life loss")
    void tappingUnenchantedLandDoesNotLoseLife() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Corrupted Roots"));
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard().getName().equals("Corrupted Roots"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Resolving the Aura attaches it to its target without tapping that land")
    void resolvingAuraAttachesToLand() {
        harness.addToBattlefield(player2, new Plains());
        Permanent land = findPermanent(player2, "Plains");
        harness.setHand(player1, List.of(new CorruptedRoots()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, land.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Corrupted Roots").getAttachedTo()).isEqualTo(land.getId());
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanting an already tapped land does not cause life loss")
    void enchantingTappedLandDoesNotTrigger() {
        harness.addToBattlefield(player2, new Forest());
        Permanent land = findPermanent(player2, "Forest");
        land.tap();
        harness.setHand(player1, List.of(new CorruptedRoots()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, land.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Corrupted Roots").getAttachedTo()).isEqualTo(land.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each new tap of the enchanted land causes another two life loss")
    void repeatedTapsLoseLifeEachTime() {
        addLandWithAura(player1);
        harness.tapPermanent(player1, 0);
        resolveAllTriggers();
        findPermanent(player1, "Forest").untap();

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The land's last controller loses life even if the land is bounced in response")
    void bouncedLandStillCausesLifeLoss() {
        addLandWithAura(player1);
        Permanent land = findPermanent(player1, "Forest");
        harness.setHand(player2, List.of(new ResoundingWave()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.tapPermanent(player1, 0);
        harness.passPriority(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.castInstant(player2, 0, land.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Removing the Aura in response does not stop its queued life loss")
    void bouncedAuraStillCausesLifeLoss() {
        addLandWithAura(player1);
        Permanent aura = findPermanent(player1, "Corrupted Roots");
        harness.setHand(player2, List.of(new ResoundingWave()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.tapPermanent(player1, 0);
        harness.passPriority(player1);
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Corrupted Roots");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    /**
     * Places a Forest on {@code owner}'s battlefield (index 0) with a Corrupted Roots attached (index 1).
     */
    private void addLandWithAura(Player owner) {
        harness.addToBattlefield(owner, new Forest());
        Permanent land = gd.playerBattlefields.get(owner.getId()).getFirst();

        CorruptedRoots auraCard = new CorruptedRoots();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }

}
