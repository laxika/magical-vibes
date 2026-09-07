package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZukosExile.class, GreaterAuramancy.class, GrizzlyBears.class, MindStone.class, Forest.class})
class ZukosExileTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and gives its controller a Clue")
    void exilesCreatureAndCreatesClueForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castZukosExile(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Can exile an artifact")
    void exilesArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castZukosExile(target);

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Can exile an enchantment")
    void exilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());

        castZukosExile(target);

        harness.assertNotOnBattlefield(player2, "Greater Auramancy");
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or enchantment");
    }

    @Test
    @DisplayName("Does not create a Clue when the target leaves before resolution")
    void doesNotCreateClueWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    private void castZukosExile(Permanent target) {
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
