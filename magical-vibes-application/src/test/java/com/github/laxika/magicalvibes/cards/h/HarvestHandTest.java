package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarvestHandTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to the battlefield transformed as Scrounged Scythe when it dies")
    void returnsTransformedOnDeath() {
        harness.addToBattlefield(player1, new HarvestHand());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Harvest Hand"));
        harness.passBothPriorities(); // resolve Lightning Bolt
        harness.passBothPriorities(); // resolve the death trigger

        Permanent returned = findPermanent(player1, "Scrounged Scythe");
        assertThat(returned.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Harvest Hand");
    }

    @Test
    @DisplayName("Does not return if it has already left the graveyard")
    void doesNotReturnIfNoLongerInGraveyard() {
        harness.addToBattlefield(player1, new HarvestHand());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Harvest Hand"));
        harness.passBothPriorities(); // resolve Lightning Bolt

        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities(); // resolve the death trigger

        assertThat(findPermanentOrNull(player1, "Scrounged Scythe")).isNull();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = addScytheReady(player1);
        scythe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped Human has menace, equipped non-Human does not")
    void menaceOnlyForHumans() {
        Permanent human = addReadyHuman(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = addScytheReady(player1);

        scythe.setAttachedTo(human.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isTrue();

        scythe.setAttachedTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches the Scythe to a creature you control")
    void equipAttachesToCreature() {
        Permanent scythe = addScytheReady(player1);
        Permanent human = addReadyHuman(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isTrue();
    }

    private Permanent addScytheReady(Player player) {
        Permanent perm = new Permanent(new HarvestHand().getBackFaceCard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyHuman(Player player) {
        Permanent perm = new Permanent(new EliteVanguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
