package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagitekScythe.class, GrizzlyBears.class})
class MagitekScytheTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent scythe = addReadyScythe(player1);
        scythe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting the ETB ability attaches the Scythe and grants first strike and must-be-blocked")
    void acceptingEtbAbilityAttachesAndGrantsTemporaryEffects() {
        Permanent creature = addReadyCreature(player1);
        castScytheTargeting(creature.getId());

        harness.passBothPriorities();
        Permanent scythe = findPermanent(player1, "Magitek Scythe");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(creature.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the Scythe unattached")
    void decliningEtbAbilityLeavesScytheUnattached() {
        Permanent creature = addReadyCreature(player1);
        castScytheTargeting(creature.getId());

        harness.passBothPriorities();
        Permanent scythe = findPermanent(player1, "Magitek Scythe");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(scythe.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(creature.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The ETB effect forces the equipped attacker to be blocked when able")
    void etbEffectRequiresAttackerToBeBlocked() {
        Permanent creature = addReadyCreature(player1);
        castScytheTargeting(creature.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        creature.setAttacking(true);
        addReadyCreature(player2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("The ETB target must be a creature controlled by the caster")
    void etbTargetMustBeControlledCreature() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new MagitekScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castScytheTargeting(UUID creatureId) {
        harness.setHand(player1, List.of(new MagitekScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0, creatureId);
    }

    private Permanent addReadyScythe(Player player) {
        return addReady(player, new MagitekScythe());
    }

    private Permanent addReadyCreature(Player player) {
        return addReady(player, new GrizzlyBears());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
