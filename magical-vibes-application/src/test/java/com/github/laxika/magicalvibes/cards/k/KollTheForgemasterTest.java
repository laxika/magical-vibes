package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(gqs.getEffectivePower(gd, equippedToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, equippedToken)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, plainToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, plainToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another enchanted or equipped nontoken creature returns to its owner's hand")
    void enchantedOrEquippedNontokenCreatureReturnsToHand() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachAura(enchanted);
        Permanent equipped = addCreatureReady(player1, new GoblinAssailant());
        attachEquipment(equipped);

        destroyAndResolve(enchanted);
        destroyAndResolve(equipped);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Goblin Assailant");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Goblin Assailant");
    }

    @Test
    @DisplayName("A stolen enchanted creature returns to its owner's hand")
    void stolenEnchantedCreatureReturnsToOwnersHand() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent stolen = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        attachAura(stolen);

        destroyAndResolve(stolen);

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unattached nontoken creature stays in its owner's graveyard")
    void unattachedNontokenCreatureStaysInGraveyard() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        destroyAndResolve(creature);

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An enchanted creature token stays in its owner's graveyard")
    void enchantedTokenStaysInGraveyard() {
        addCreatureReady(player1, new KollTheForgemaster());
        Permanent token = addToken(player1);
        attachAura(token);

        destroyAndResolve(token);

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player) {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return addCreatureReady(player, tokenCard);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = new Permanent(new Bonesplitter());
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(equipment);
    }

    private void destroyAndResolve(Permanent creature) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature));
        resolveAllTriggers();
    }
}
