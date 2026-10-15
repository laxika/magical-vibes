package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.cards.v.VaporSnag;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParasiticImplant.class, SpinedThopter.class, BeastWithin.class, VaporSnag.class})
class ParasiticImplantTest extends BaseCardTest {

    @Test
    @DisplayName("At controller's upkeep, enchanted creature is sacrificed and a Myr token is created")
    void upkeepSacrificesCreatureAndCreatesToken() {
        Permanent creature = addCreatureReady(player2, new SpinedThopter());

        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Confirm creature is enchanted
        Permanent auraPerm = findPermanent(player1, "Parasitic Implant");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(creature.getId());

        // Advance to player1's upkeep (aura controller)
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Enchanted creature should be gone
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isFalse();

        // Creature should be in graveyard
        harness.assertInGraveyard(player2, "Spined Thopter");

        // Aura should also be gone (orphaned)
        harness.assertNotOnBattlefield(player1, "Parasitic Implant");

        // A 1/1 Phyrexian Myr artifact creature token should exist for player1
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Myr")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().hasType(CardType.ARTIFACT)
                        && p.getCard().isToken())).isTrue();
        assertThat(countPermanents(player1, "Phyrexian Myr")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Phyrexian Myr");
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.MYR);
        assertThat(token.getCard().getColors()).isEmpty();
        harness.assertNotOnBattlefield(player2, "Phyrexian Myr");
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2, new SpinedThopter());

        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Advance to player2's upkeep (not the aura controller)
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // Creature should still be alive
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getId().equals(creature.getId()))).isTrue();

        // No Myr token should exist
        harness.assertNotOnBattlefield(player1, "Phyrexian Myr");
    }

    @Test
    @DisplayName("Enchanting own creature — sacrifice and token work for same player")
    void enchantingOwnCreature() {
        Permanent creature = addCreatureReady(player1, new SpinedThopter());

        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Own creature should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getId().equals(creature.getId()))).isTrue();

        // Should have a Myr token
        harness.assertOnBattlefield(player1, "Phyrexian Myr");
    }

    @Test
    @DisplayName("Removing the Aura after its upkeep trigger does not stop sacrifice or token creation")
    void removingAuraInResponseDoesNotStopTrigger() {
        Permanent creature = addCreatureReady(player2, new SpinedThopter());
        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Parasitic Implant");

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Parasitic Implant");
        harness.assertOnBattlefield(player2, "Spined Thopter");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spined Thopter");
        harness.assertNotOnBattlefield(player2, "Spined Thopter");
        assertThat(countPermanents(player1, "Phyrexian Myr")).isEqualTo(1);
    }

    @Test
    @DisplayName("A Myr is still created if the enchanted creature leaves in response")
    void createsTokenWhenCreatureLeavesInResponse() {
        Permanent creature = addCreatureReady(player2, new SpinedThopter());
        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Spined Thopter");
        harness.assertInGraveyard(player1, "Parasitic Implant");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Myr")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Spined Thopter");
        harness.assertInHand(player2, "Spined Thopter");
    }
}
