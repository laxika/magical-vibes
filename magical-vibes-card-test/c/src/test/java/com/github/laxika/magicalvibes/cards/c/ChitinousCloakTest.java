package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitinousCloak.class, GrizzlyBears.class})
class ChitinousCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and menace")
    void equippedCreatureGetsBoostAndMenace() {
        Permanent creature = addCreatureReady(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Only the equipped creature gets the boost and menace")
    void onlyEquippedCreatureGetsGrants() {
        Permanent equippedCreature = addCreatureReady(player1);
        Permanent otherCreature = addCreatureReady(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Re-equipping removes the grants from the previous creature")
    void reEquippingMovesGrants() {
        Permanent cloak = addCloakReady(player1);
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        cloak.setAttachedTo(firstCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches an unattached cloak for three generic mana")
    void equipAttachesCloak() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(cloak.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addCloakReady(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("An equip target leaving does not detach the cloak from its previous creature")
    void failedReEquipKeepsPreviousAttachment() {
        Permanent cloak = addCloakReady(player1);
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        cloak.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(secondCreature);
        gd.playerGraveyards.get(player1.getId()).add(secondCreature.getCard());

        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Granted menace requires at least two blockers")
    void menaceRequiresTwoBlockers() {
        Permanent creature = addCreatureReady(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());
        addCreatureReady(player2);
        addCreatureReady(player2);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addCloakReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChitinousCloak());
        perm.setSummoningSick(false);
        return perm;
    }
}
