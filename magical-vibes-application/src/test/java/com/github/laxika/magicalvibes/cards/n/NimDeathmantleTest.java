package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NimDeathmantleTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Equipped creature gets +2/+2 and intimidate")
    void equippedCreatureGetsBoostAndIntimidate() {
        Permanent deathmantle = new Permanent(new NimDeathmantle());
        gd.playerBattlefields.get(player1.getId()).add(deathmantle);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Attach equipment
        deathmantle.setAttachedTo(bears.getId());

        // Verify +2/+2 boost
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4); // 2 + 2

        // Verify intimidate
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gains black color and Zombie subtype via static bonus")
    void equippedCreatureGainsColorAndSubtype() {
        Permanent deathmantle = new Permanent(new NimDeathmantle());
        gd.playerBattlefields.get(player1.getId()).add(deathmantle);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Attach equipment
        deathmantle.setAttachedTo(bears.getId());

        // Verify color and subtype via static bonus
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedColors()).contains(CardColor.BLACK);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(bonus.colorOverriding()).isTrue();
        assertThat(bonus.subtypeOverriding()).isTrue();
    }

    @Test
    @DisplayName("Static effects removed when equipment is unequipped")
    void staticEffectsRemovedWhenUnequipped() {
        Permanent deathmantle = new Permanent(new NimDeathmantle());
        gd.playerBattlefields.get(player1.getId()).add(deathmantle);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Attach then detach
        deathmantle.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        deathmantle.setAttachedTo(null);

        // Static bonuses should no longer apply
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedColors()).doesNotContain(CardColor.BLACK);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(bonus.colorOverriding()).isFalse();
        assertThat(bonus.subtypeOverriding()).isFalse();
    }

    @Test
    @DisplayName("Death trigger fires when own nontoken creature dies, returns it with equipment attached")
    void deathTriggerReturnsCreatureWithEquipmentAttached() {
        Permanent deathmantle = new Permanent(new NimDeathmantle());
        gd.playerBattlefields.get(player1.getId()).add(deathmantle);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Give player1 mana to pay for the trigger
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Kill own creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Resolve Shock, creature dies

        // Should be prompted with may ability to pay {4}
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Accept and pay
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // Resolve the return effect

        // Creature should be back on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        // Equipment should be attached to the returned creature
        Permanent returnedBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(returnedBears).isNotNull();

        Permanent nimDeathmantle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nim Deathmantle"))
                .findFirst().orElse(null);
        assertThat(nimDeathmantle).isNotNull();
        assertThat(nimDeathmantle.getAttachedTo()).isEqualTo(returnedBears.getId());
    }

    @Test
    @DisplayName("Death trigger does not fire for opponent's creatures")
    void deathTriggerDoesNotFireForOpponentCreatures() {
        Permanent deathmantle = new Permanent(new NimDeathmantle());
        gd.playerBattlefields.get(player1.getId()).add(deathmantle);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opponentBears.getId());
        harness.passBothPriorities(); // Resolve Shock, opponent's creature dies

        // Should NOT be prompted with may ability (opponent's creature, not in our graveyard)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Death trigger allows declining to pay")
    void deathTriggerDeclineToPay() {
        Permanent deathmantle = new Permanent(new NimDeathmantle());
        gd.playerBattlefields.get(player1.getId()).add(deathmantle);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Kill the creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        // Creature should stay in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
