package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NimDeathmantleTest extends BaseCardTest {

    @Test
    @DisplayName("Nim Deathmantle has correct static effects")
    void hasCorrectStaticEffects() {
        NimDeathmantle card = new NimDeathmantle();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(4);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(StaticBoostEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect keywordEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(keywordEffect.keywords()).containsExactly(Keyword.INTIMIDATE);
        assertThat(keywordEffect.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
        assertThat(card.getEffects(EffectSlot.STATIC).get(2)).isInstanceOf(GrantColorEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(3)).isInstanceOf(GrantSubtypeEffect.class);
    }

    @Test
    @DisplayName("Nim Deathmantle has death trigger")
    void hasDeathTrigger() {
        NimDeathmantle card = new NimDeathmantle();

        assertThat(card.getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES).getFirst())
                .isInstanceOf(MayPayManaEffect.class);
        MayPayManaEffect mayPay = (MayPayManaEffect) card.getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES).getFirst();
        assertThat(mayPay.manaCost()).isEqualTo("{4}");
        assertThat(mayPay.wrapped()).isInstanceOf(ReturnDyingCreatureToBattlefieldAndAttachSourceEffect.class);
    }

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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

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
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        // Creature should stay in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
