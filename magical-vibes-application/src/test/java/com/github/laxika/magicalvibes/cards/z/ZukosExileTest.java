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

    @Test
    @DisplayName("Can exile its controller's own permanent and give them the Clue")
    void exilesOwnPermanentAndCreatesClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MindStone());

        castZukosExile(target);

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mind Stone"));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void createdClueCanDrawCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ZukosExile()));

        castZukosExile(target);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);

        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player2, "Zuko's Exile");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The Clue goes to the target's controller while the card is exiled for its owner")
    void createsClueForControllerRatherThanOwner() {
        MindStone stolenArtifact = new MindStone();
        stolenArtifact.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, stolenArtifact);

        castZukosExile(target);

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(stolenArtifact.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Exiling a Clue token still gives its controller a new Clue")
    void exilesTokenAndCreatesNewClue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castZukosExile(target);
        Permanent clue = findPermanents(player2, "Clue").getFirst();

        castZukosExile(clue);

        assertThat(findPermanents(player2, "Clue")).hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(clue.getId()));
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A target that gains shroud before resolution is not exiled and creates no Clue")
    void targetGainingShroudPreventsBothEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player2, new GreaterAuramancy());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Greater Auramancy")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void castZukosExile(Permanent target) {
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
