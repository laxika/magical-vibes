package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaiMasterThopteristTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact spell creates a 1/1 Thopter token with flying")
    void artifactCastCreatesThopter() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve the trigger and the artifact

        List<Permanent> tokens = findPermanents(player1, "Thopter");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a nonartifact spell creates no Thopter")
    void nonArtifactCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two artifacts draws a card")
    void sacrificeTwoArtifactsDrawsCard() {
        Permanent sai = harness.addToBattlefieldAndReturn(player1, new SaiMasterThopterist());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, indexOf(sai), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Memnite"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate the draw ability without two artifacts to sacrifice")
    void cannotActivateWithoutTwoArtifacts() {
        Permanent sai = harness.addToBattlefieldAndReturn(player1, new SaiMasterThopterist());
        harness.addToBattlefield(player1, new Memnite());

        harness.addMana(player1, ManaColor.BLUE, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sai), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
