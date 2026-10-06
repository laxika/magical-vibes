package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParcelMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShatteredEgo.class, GrizzlyBears.class, ParcelMyr.class, SimicGuildmage.class})
class ShatteredEgoTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = addCreatureReady(player2);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability puts the enchanted creature third from the top of its owner's library")
    void abilityPutsEnchantedCreatureThirdFromTop() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, library);

        activateAura(aura);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).get(2)).isSameAs(creature.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void castingAuraAttachesItAndDebuffsOnlyItsHost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        harness.setHand(player1, List.of(new ShatteredEgo()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shattered Ego").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void abilityPutsCreatureOnBottomOfShortLibrary(int librarySize) {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        Permanent aura = attachAura(player1, creature);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < librarySize; i++) {
            library.add(new ParcelMyr());
        }
        harness.setLibrary(player2, library);

        activateAura(aura);
        harness.passBothPriorities();

        List<Card> expected = new ArrayList<>(library);
        expected.add(creature.getOriginalCard());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(expected);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Shattered Ego");
    }

    @Test
    void abilityUsesOwnersLibraryRatherThanControllersLibrary() {
        ParcelMyr card = new ParcelMyr();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        Permanent aura = attachAura(player1, creature);
        List<Card> ownersLibrary = List.of(new ParcelMyr(), new ParcelMyr(), new ParcelMyr());
        List<Card> controllersLibrary = List.of(new ParcelMyr());
        harness.setLibrary(player1, ownersLibrary);
        harness.setLibrary(player2, controllersLibrary);

        activateAura(aura);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                ownersLibrary.get(0), ownersLibrary.get(1), card, ownersLibrary.get(2));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(controllersLibrary);
    }

    @Test
    void abilityStillResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        Permanent aura = attachAura(player1, creature);
        harness.setLibrary(player2, List.of());
        activateAura(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature.getOriginalCard());
        harness.assertInHand(player1, "Shattered Ego");
        harness.assertNotInGraveyard(player1, "Shattered Ego");
    }

    @Test
    void abilityDoesNothingIfEnchantedCreatureLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        Permanent aura = attachAura(player1, creature);
        List<Card> library = List.of(new ParcelMyr());
        harness.setLibrary(player2, library);
        activateAura(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        harness.assertInHand(player2, "Parcel Myr");
        harness.assertInGraveyard(player1, "Shattered Ego");
    }

    @Test
    @CardUsed({ShatteredEgo.class, ParcelMyr.class, SimicGuildmage.class})
    void abilityUsesCreatureEnchantedAtResolutionAfterAuraMoves() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new SimicGuildmage());
        Permanent originalHost = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        Permanent newHost = harness.addToBattlefieldAndReturn(player2, new ParcelMyr());
        Permanent aura = attachAura(player1, originalHost);
        harness.setLibrary(player2, List.of());
        activateAura(aura);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guildmage),
                1, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(newHost.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(newHost.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalHost).doesNotContain(newHost);
        harness.assertInGraveyard(player1, "Shattered Ego");
    }

    private void activateAura(Permanent aura) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ShatteredEgo());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
