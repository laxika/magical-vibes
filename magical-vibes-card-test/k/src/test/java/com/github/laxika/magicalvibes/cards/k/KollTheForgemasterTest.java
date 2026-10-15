package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.b.BoundInGold;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.i.ImmersturmRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KollTheForgemaster.class, GoldveinPick.class, ImmersturmRaider.class,
        AxgardCavalry.class, BoundInGold.class})
class KollTheForgemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted or equipped creature tokens get +1/+1")
    void enchantedOrEquippedTokensGetBoost() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent enchantedToken = addToken(player1);
        attachAura(enchantedToken);
        Permanent equippedToken = addToken(player1);
        attachEquipment(equippedToken);
        Permanent plainToken = addToken(player1);
        Permanent opponentToken = addToken(player2);

        assertThat(gqs.getEffectivePower(gd, enchantedToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedToken)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, equippedToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, equippedToken)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, plainToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, plainToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another enchanted or equipped nontoken creature returns to its owner's hand")
    void enchantedOrEquippedNontokenCreatureReturnsToHand() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent enchanted = addCreatureReady(player1, new AxgardCavalry());
        attachAura(enchanted);
        Permanent equipped = addCreatureReady(player1, new ImmersturmRaider());
        attachEquipment(equipped);

        destroyAndResolve(enchanted);
        destroyAndResolve(equipped);

        harness.assertInHand(player1, "Axgard Cavalry");
        harness.assertInHand(player1, "Immersturm Raider");
        harness.assertNotInGraveyard(player1, "Axgard Cavalry");
        harness.assertNotInGraveyard(player1, "Immersturm Raider");
    }

    @Test
    @DisplayName("A stolen enchanted creature returns to its owner's hand")
    void stolenEnchantedCreatureReturnsToOwnersHand() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent stolen = addCreatureReady(player1, new AxgardCavalry());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        attachAura(stolen);

        destroyAndResolve(stolen);

        harness.assertNotInHand(player1, "Axgard Cavalry");
        harness.assertInHand(player2, "Axgard Cavalry");
    }

    @Test
    @DisplayName("An unattached nontoken creature stays in its owner's graveyard")
    void unattachedNontokenCreatureStaysInGraveyard() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());

        destroyAndResolve(creature);

        harness.assertNotInHand(player1, "Axgard Cavalry");
        harness.assertInGraveyard(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("An enchanted creature token does not trigger a return to hand")
    void enchantedTokenDoesNotTriggerReturnToHand() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent token = addToken(player1);
        attachAura(token);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, token));

        harness.assertNotInHand(player1, "Axgard Cavalry");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An equipped creature token does not trigger a return to hand")
    void equippedTokenDoesNotTriggerReturnToHand() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent token = addToken(player1);
        attachEquipment(token);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, token));

        harness.assertNotInHand(player1, "Axgard Cavalry");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Several attachments grant only one Koll bonus")
    void severalAttachmentsGrantOnlyOneBonus() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent token = addToken(player1);
        attachAura(token);
        attachAura(token);
        attachEquipment(token);
        attachEquipment(token);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent-controlled attachments qualify your tokens, but opposing tokens get no bonus")
    void attachmentOwnershipDoesNotRestrictBonus() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent ownToken = addToken(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BoundInGold());
        aura.setAttachedTo(ownToken.getId());
        Permanent opponentToken = addToken(player2);
        attachAura(opponentToken);
        attachEquipment(opponentToken);

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nontoken creatures get no Koll bonus even when both enchanted and equipped")
    void attachedNontokenGetsNoBonus() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        attachAura(creature);
        attachEquipment(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Moving the only equipment moves Koll's token bonus")
    void movingEquipmentMovesBonus() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent first = addToken(player1);
        Permanent second = addToken(player1);
        attachEquipment(first);
        Permanent equipment = findPermanent(player1, "Goldvein Pick");

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        equipment.setAttachedTo(second.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Koll does not return itself even when enchanted")
    void kollDoesNotReturnItself() {
        Permanent koll = addCreatureReady(player1, new KollTheForgemaster());
        attachAura(koll);

        destroyAndResolve(koll);

        harness.assertInGraveyard(player1, "Koll, the Forgemaster");
        harness.assertNotInHand(player1, "Koll, the Forgemaster");
    }

    @Test
    @DisplayName("An opponent's enchanted creature does not return")
    void opposingCreatureDoesNotReturn() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player2, new AxgardCavalry());
        attachAura(creature);

        destroyAndResolve(creature);

        harness.assertInGraveyard(player2, "Axgard Cavalry");
        harness.assertNotInHand(player2, "Axgard Cavalry");
    }

    @Test
    @DisplayName("Destroying the attachment after death does not prevent the return")
    void attachmentRemovedAfterDeathDoesNotPreventReturn() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        attachEquipment(creature);
        Permanent equipment = findPermanent(player1, "Goldvein Pick");

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature);
            harness.getPermanentRemovalService().tryDestroyPermanent(gd, equipment);
        });
        resolveAllTriggers();

        harness.assertInHand(player1, "Axgard Cavalry");
        harness.assertNotInGraveyard(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("A creature dying simultaneously with its Aura and Koll still returns")
    void simultaneousAuraAndCreatureDeathReturnsCreature() {
        Permanent koll = addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        attachAura(creature);
        Permanent aura = findPermanent(player1, "Bound in Gold");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(aura, koll, creature), () -> {
                    harness.getPermanentRemovalService().tryDestroyPermanent(gd, aura);
                    harness.getPermanentRemovalService().tryDestroyPermanent(gd, koll);
                    harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature);
                }));
        resolveAllTriggers();

        harness.assertInHand(player1, "Axgard Cavalry");
        harness.assertNotInGraveyard(player1, "Axgard Cavalry");
        harness.assertInGraveyard(player1, "Koll, the Forgemaster");
    }

    @Test
    @DisplayName("A creature dying simultaneously with its Equipment still returns")
    void simultaneousEquipmentAndCreatureDeathReturnsCreature() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        attachEquipment(creature);
        Permanent equipment = findPermanent(player1, "Goldvein Pick");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(equipment, creature), () -> {
                    harness.getPermanentRemovalService().tryDestroyPermanent(gd, equipment);
                    harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature);
                }));
        resolveAllTriggers();

        harness.assertInHand(player1, "Axgard Cavalry");
        harness.assertNotInGraveyard(player1, "Axgard Cavalry");
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player) {
        Card tokenCard = new AxgardCavalry();
        tokenCard.setToken(true);
        return addCreatureReady(player, tokenCard);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BoundInGold());
        aura.setAttachedTo(creature.getId());
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        equipment.setAttachedTo(creature.getId());
    }

    private void destroyAndResolve(Permanent creature) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature));
        resolveAllTriggers();
    }
}
