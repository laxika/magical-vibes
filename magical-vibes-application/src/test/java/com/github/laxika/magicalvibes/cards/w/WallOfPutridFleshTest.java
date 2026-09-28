package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfPutridFlesh.class, SpiritLink.class, PsionicEntity.class,
        BarbaryApes.class, TundraWolves.class, ChainLightning.class})
class WallOfPutridFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from an enchanted creature")
    void preventsDamageFromEnchantedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        Permanent psionicEntity = addCreatureReady(player1, new PsionicEntity());
        attachSpiritLink(psionicEntity);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from an unenchanted creature")
    void doesNotPreventDamageFromUnenchantedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        addCreatureReady(player1, new PsionicEntity());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents combat damage from an enchanted creature")
    void preventsCombatDamageFromEnchantedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        Permanent attacker = addCreatureReady(player1, new BarbaryApes());
        attachSpiritLink(attacker);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Protection from white prevents damage from a white creature")
    void protectionFromWhitePreventsWhiteCreatureDamage() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        addCreatureReady(player1, new TundraWolves());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from a noncreature source")
    void doesNotPreventDamageFromNoncreatureSource() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        harness.handleMayAbilityChosen(player2, false);
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        harness.setHand(player1, List.of(new SpiritLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    private void attachSpiritLink(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiritLink());
        aura.setAttachedTo(creature.getId());
    }
}
