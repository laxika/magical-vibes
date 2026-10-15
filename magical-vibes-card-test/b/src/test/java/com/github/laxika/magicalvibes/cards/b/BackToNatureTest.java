package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ElixirOfImmortality;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BackToNature.class, AngelicChorus.class, GrizzlyBears.class,
        HolyStrength.class, RuleOfLaw.class, ElixirOfImmortality.class})
class BackToNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Destroys a single enchantment")
    void destroysSingleEnchantment() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertInGraveyard(player1, "Rule of Law");
    }

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures")
    void destroysAurasAttachedToCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        auraPerm.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        // Aura is destroyed
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Holy Strength");
        // Creature survives
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when no enchantments on battlefield")
    void doesNothingWhenNoEnchantments() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Back to Nature goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Back to Nature");
    }

    @Test
    @DisplayName("Destroys every enchantment while leaving artifacts and creatures intact")
    void destroysAllEnchantmentsOnMixedBattlefields() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BackToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Elixir of Immortality");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves enchantment cards in hands, libraries and graveyards untouched")
    void doesNotAffectEnchantmentCardsOutsideBattlefield() {
        AngelicChorus libraryCard = new AngelicChorus();
        RuleOfLaw graveyardCard = new RuleOfLaw();
        harness.setHand(player1, List.of(new BackToNature(), new HolyStrength()));
        harness.setHand(player2, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Holy Strength");
        harness.assertInHand(player2, "Holy Strength");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .contains(graveyardCard).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }
}
