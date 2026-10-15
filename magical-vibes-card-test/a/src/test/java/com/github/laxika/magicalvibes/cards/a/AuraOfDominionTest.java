package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KamiOfTheHunt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraOfDominion.class, KamiOfTheHunt.class})
class AuraOfDominionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting the Aura attaches it to an opponent's creature and its controller can untap that creature")
    void enchantsAndUntapsOpponentsCreature() {
        Permanent enchanted = addCreatureReady(player2, new KamiOfTheHunt());
        enchanted.tap();
        Permanent tapFodder = addCreatureReady(player1, new KamiOfTheHunt());
        harness.setHand(player1, List.of(new AuraOfDominion()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Aura of Dominion");
        assertThat(aura.getAttachedTo()).isEqualTo(enchanted.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        assertThat(tapFodder.isTapped()).isTrue();
        assertThat(enchanted.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the written tap cost")
    void summoningSickCreatureCanPayCost() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new KamiOfTheHunt());
        enchanted.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        assertThat(enchanted.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without mana even when an untapped creature is available")
    void cannotActivateWithoutMana() {
        Permanent enchanted = addCreatureReady(player1, new KamiOfTheHunt());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps the creature currently enchanted when the Aura moves before resolution")
    void usesCurrentAttachmentAtResolution() {
        Permanent original = addCreatureReady(player2, new KamiOfTheHunt());
        original.tap();
        Permanent replacement = addCreatureReady(player2, new KamiOfTheHunt());
        replacement.tap();
        Permanent tapFodder = addCreatureReady(player1, new KamiOfTheHunt());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        aura.setAttachedTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(original.isTapped()).isTrue();
        assertThat(replacement.isTapped()).isFalse();
        assertThat(tapFodder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability untaps the enchanted creature and taps the creature paid as a cost")
    void untapsEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new KamiOfTheHunt());
        enchanted.tap();
        Permanent tapFodder = addCreatureReady(player1, new KamiOfTheHunt());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIdx, null, null);
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(tapFodder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature itself can be tapped to pay the cost")
    void enchantedCreatureCanPayItsOwnCost() {
        Permanent enchanted = addCreatureReady(player1, new KamiOfTheHunt());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIdx, null, null);
        harness.passBothPriorities();

        // Tapped as a cost, then untapped on resolution.
        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent enchanted = addCreatureReady(player1, new KamiOfTheHunt());
        enchanted.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures an opponent controls cannot pay the cost")
    void opponentCreaturesCannotPayCost() {
        Permanent enchanted = addCreatureReady(player1, new KamiOfTheHunt());
        enchanted.tap();
        addCreatureReady(player2, new KamiOfTheHunt());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the last known enchanted creature if the Aura leaves before resolution")
    void usesLastKnownEnchantedCreatureWhenAuraLeavesBeforeResolution() {
        Permanent enchanted = addCreatureReady(player1, new KamiOfTheHunt());
        enchanted.tap();
        Permanent tapFodder = addCreatureReady(player1, new KamiOfTheHunt());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIdx, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(tapFodder.isTapped()).isTrue();
    }
}
