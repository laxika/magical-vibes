package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.j.JeweledAmulet;
import com.github.laxika.magicalvibes.cards.o.OathOfLimDL;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceVortex.class, BalduvianBears.class, JeweledAmulet.class, OathOfLimDL.class})
class EssenceVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays life equal to the creature's toughness and it survives")
    void controllerPaysToughnessCreatureSurvives() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);
        castVortexOn(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        // Balduvian Bears is 2/2 — two life.
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Controller declines and the creature is destroyed")
    void controllerDeclinesCreatureDestroyed() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);
        castVortexOn(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A controller with too little life can't pay and the creature is destroyed automatically")
    void cannotPayDestroysAutomatically() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 1);
        castVortexOn(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 1);
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A creature destroyed this way can't be regenerated")
    void destroyedCreatureCannotRegenerate() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        target.setRegenerationShield(1);
        harness.setLife(player2, 20);
        castVortexOn(target);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Boosted toughness raises the life the controller must pay")
    void boostedToughnessRaisesLifeCost() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        target.setToughnessModifier(3);
        harness.setLife(player2, 20);
        castVortexOn(target);

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Toughness at resolution determines the life cost")
    void toughnessAtResolutionDeterminesLifeCost() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);
        castVortexOnStack(target);

        target.setToughnessModifier(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("An indestructible creature survives when its controller declines")
    void indestructibleCreatureSurvivesDestruction() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setLife(player2, 20);
        castVortexOn(target);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Paying life triggers abilities that trigger when a player loses life")
    void payingLifeTriggersLifeLossAbilities() {
        harness.addToBattlefield(player2, new OathOfLimDL());
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new JeweledAmulet());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        castVortexOn(target);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JeweledAmulet());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new EssenceVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castVortexOn(Permanent target) {
        castVortexOnStack(target);
        harness.passBothPriorities();
    }

    private void castVortexOnStack(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
    }
}
