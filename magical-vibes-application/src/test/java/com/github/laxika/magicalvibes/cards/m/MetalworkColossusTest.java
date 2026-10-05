package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetalworkColossus.class, MindStone.class, DarksteelIngot.class, MyrEnforcer.class, LightningAxe.class})
class MetalworkColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifacts reduce the generic casting cost by their total mana value")
    void noncreatureArtifactsReduceCastingCostByTotalManaValue() {
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new MyrEnforcer());
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new MetalworkColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrificing two artifacts returns Metalwork Colossus from the graveyard to hand")
    void sacrificingTwoArtifactsReturnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new MetalworkColossus()));
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new DarksteelIngot());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Metalwork Colossus");
        harness.assertNotInGraveyard(player1, "Metalwork Colossus");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A discount greater than eleven allows casting without mana")
    void discountCannotReduceCostBelowZero() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new DarksteelIngot());
        }
        harness.setHand(player1, List.of(new MetalworkColossus()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Metalwork Colossus");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without noncreature artifacts the full eleven mana is required")
    void artifactCreaturesDoNotProvideADiscount() {
        harness.addToBattlefield(player1, new MetalworkColossus());
        harness.setHand(player1, List.of(new MetalworkColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Metalwork Colossus");
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact creatures may pay the cost, but only the activating graveyard card returns")
    void sacrificesArePaidBeforeResolutionAndReturnOnlySource() {
        MetalworkColossus source = new MetalworkColossus();
        MetalworkColossus other = new MetalworkColossus();
        harness.setGraveyard(player1, List.of(source, other));
        harness.addToBattlefield(player1, new MetalworkColossus());
        harness.addToBattlefield(player1, new MetalworkColossus());

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(source, other);
        harness.assertNotInHand(player1, "Metalwork Colossus");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(source).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(other).doesNotContain(source);
    }

    @Test
    @DisplayName("An opponent's artifact cannot supply the second sacrifice")
    void cannotActivateWithOnlyOneArtifactYouControl() {
        harness.setGraveyard(player1, List.of(new MetalworkColossus()));
        harness.addToBattlefield(player1, new MetalworkColossus());
        harness.addToBattlefield(player2, new MetalworkColossus());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Metalwork Colossus");
        harness.assertOnBattlefield(player2, "Metalwork Colossus");
        harness.assertInGraveyard(player1, "Metalwork Colossus");
        harness.assertNotInHand(player1, "Metalwork Colossus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An older activation cannot return Colossus after it leaves and reenters the graveyard")
    void olderActivationCannotReturnANewGraveyardObject() {
        MetalworkColossus source = new MetalworkColossus();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(new LightningAxe()));
        var first = harness.addToBattlefieldAndReturn(player1, new MetalworkColossus());
        var second = harness.addToBattlefieldAndReturn(player1, new MetalworkColossus());
        harness.addToBattlefield(player1, new MetalworkColossus());
        harness.addToBattlefield(player1, new MetalworkColossus());
        var target = harness.addToBattlefieldAndReturn(player2, new MetalworkColossus());

        harness.activateGraveyardAbility(player1, 0);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(source);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(source);
    }
}
