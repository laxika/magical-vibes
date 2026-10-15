package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeatherseedTotem.class, Disenchant.class, LightningBolt.class})
class WeatherseedTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Weatherseed Totem adds green mana")
    void tappingAddsGreenMana() {
        Permanent totem = addReadyTotem();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Weatherseed Totem becomes a green 5/3 Treefolk artifact creature with trample")
    void animatesIntoTreefolk() {
        Permanent totem = addReadyTotem();
        animate();

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.isArtifact(totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, totem)).containsExactly(CardColor.GREEN);
        assertThat(totem.getTransientSubtypes()).contains(CardSubtype.TREEFOLK);
        assertThat(gqs.hasKeyword(gd, totem, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Weatherseed Totem stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent totem = addReadyTotem();
        animate();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isFalse();
    }

    @Test
    @DisplayName("An animated Weatherseed Totem returns to its owner's hand when it dies")
    void animatedTotemReturnsToHandWhenItDies() {
        Permanent totem = addReadyTotem();
        animate();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, totem.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Weatherseed Totem");
        harness.assertNotInGraveyard(player1, "Weatherseed Totem");
    }

    @Test
    @DisplayName("A noncreature Weatherseed Totem stays in its owner's graveyard when it dies")
    void noncreatureTotemStaysInGraveyardWhenItDies() {
        Permanent totem = addReadyTotem();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, totem.getId());

        harness.assertInGraveyard(player1, "Weatherseed Totem");
        harness.assertNotInHand(player1, "Weatherseed Totem");
    }

    @Test
    @DisplayName("A newly entered noncreature Totem can tap for mana and animate while tapped")
    void newlyEnteredTotemCanTapThenAnimate() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new WeatherseedTotem());

        harness.activateAbility(player1, 0, 0, null, null);
        animate();

        assertThat(totem.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(3);
    }

    @Test
    @DisplayName("A newly entered animated Totem cannot tap for mana")
    void newlyEnteredAnimatedTotemCannotTapForMana() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new WeatherseedTotem());
        animate();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(totem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Totem retains its mana ability")
    void animatedTotemCanTapForMana() {
        Permanent totem = addReadyTotem();
        animate();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(totem.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroying an animated Totem as an artifact returns it to hand")
    void animatedTotemDestroyedAsArtifactReturnsToHand() {
        Permanent totem = addReadyTotem();
        animate();
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, totem.getId());
        harness.assertInGraveyard(player1, "Weatherseed Totem");
        harness.assertNotInHand(player1, "Weatherseed Totem");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Weatherseed Totem");
        harness.assertNotInGraveyard(player1, "Weatherseed Totem");
    }

    @Test
    @DisplayName("A stolen animated Totem returns to its owner's hand")
    void stolenAnimatedTotemReturnsToOwner() {
        Permanent totem = addReadyTotem();
        animate();
        gd.playerBattlefields.get(player1.getId()).remove(totem);
        gd.playerBattlefields.get(player2.getId()).add(totem);
        gd.stolenCreatures.put(totem.getId(), player1.getId());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, totem.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Weatherseed Totem");
        harness.assertNotInHand(player2, "Weatherseed Totem");
        harness.assertNotInGraveyard(player1, "Weatherseed Totem");
        harness.assertNotInGraveyard(player2, "Weatherseed Totem");
    }

    @Test
    @DisplayName("A Totem destroyed after its animation expires does not return to hand")
    void expiredAnimationDoesNotEnableReturnToHand() {
        Permanent totem = addReadyTotem();
        animate();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, totem)).isFalse();
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, totem.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Weatherseed Totem");
        harness.assertNotInHand(player1, "Weatherseed Totem");
    }

    private Permanent addReadyTotem() {
        return addCreatureReady(player1, new WeatherseedTotem());
    }

    private void animate() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
