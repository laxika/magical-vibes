package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManrikiGusariTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent manrikiGusari = addManrikiGusariReady(player1);
        manrikiGusari.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature can tap to destroy target Equipment")
    void equippedCreatureDestroysTargetEquipment() {
        Permanent creature = addCreatureReady(player1);
        Permanent manrikiGusari = addManrikiGusariReady(player1);
        manrikiGusari.setAttachedTo(creature.getId());
        Permanent target = addEquipment(player2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature cannot target a non-Equipment artifact")
    void cannotTargetNonEquipmentArtifact() {
        Permanent creature = addCreatureReady(player1);
        Permanent manrikiGusari = addManrikiGusariReady(player1);
        manrikiGusari.setAttachedTo(creature.getId());
        Permanent target = addNonEquipmentArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addManrikiGusariReady(Player player) {
        Permanent perm = new Permanent(new ManrikiGusari());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipment(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addNonEquipmentArtifact(Player player) {
        com.github.laxika.magicalvibes.model.Card card = new com.github.laxika.magicalvibes.model.Card();
        card.setName("Artifact");
        card.setType(com.github.laxika.magicalvibes.model.CardType.ARTIFACT);
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
