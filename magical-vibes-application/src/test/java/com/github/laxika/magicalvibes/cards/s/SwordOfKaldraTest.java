package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoshIronGolem;
import com.github.laxika.magicalvibes.cards.c.CullingScales;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfKaldra.class, BoshIronGolem.class, CullingScales.class, FangrenHunter.class})
class SwordOfKaldraTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {4} attaches Sword of Kaldra to a creature you control")
    void equipAttachesToCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new FangrenHunter());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +5/+5")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new FangrenHunter());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    @DisplayName("Whenever equipped creature deals damage to a creature, that creature is exiled")
    void damagedCreatureIsExiled() {
        Permanent bosh = addCreatureReady(player1, new BoshIronGolem());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(bosh.getId());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent target = addCreatureReady(player2, new FangrenHunter());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrificedArtifact.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("The damage trigger does nothing if the damaged creature is no longer on the battlefield")
    void damagedCreatureMustStillBeOnBattlefield() {
        Permanent bosh = addCreatureReady(player1, new BoshIronGolem());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(bosh.getId());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent target = addCreatureReady(player2, new FangrenHunter());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrificedArtifact.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("Damage from an unequipped creature does not trigger Sword of Kaldra")
    void damageFromUnequippedCreatureDoesNotTrigger() {
        Permanent bosh = addCreatureReady(player1, new BoshIronGolem());
        Permanent sword = addSwordReady(player1);
        Permanent equippedCreature = addCreatureReady(player1, new FangrenHunter());
        sword.setAttachedTo(equippedCreature.getId());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent target = addCreatureReady(player2, new FangrenHunter());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrificedArtifact.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = new Permanent(new SwordOfKaldra());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }
}
