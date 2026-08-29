package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlobadIronGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact adds red mana equal to its mana value")
    void sacrificeAddsManaEqualToManaValue() {
        addReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Myr Battlesphere");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(7);
    }

    @Test
    @DisplayName("Artifact-only red mana can cast an artifact spell")
    void artifactOnlyManaCastsArtifactSpell() {
        addReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(5);
    }

    @Test
    @DisplayName("Artifact-only red mana cannot cast a nonartifact spell")
    void artifactOnlyManaCannotCastNonartifactSpell() {
        addReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(7);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
