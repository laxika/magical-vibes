package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.e.EdgewallPack;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.cards.v.VerazolTheSplitCurrent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CandyGrapple.class, PropheticPrism.class, EdgewallPack.class, Gingerbrute.class, UpTheBeanstalk.class})
class CandyGrappleTest extends BaseCardTest {

    @Test
    void givesTargetCreatureMinusThreeMinusThreeWithoutBargain() {
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(5);
        castCandyGrapple(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void givesTargetCreatureMinusFiveMinusFiveWithBargain() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(5);
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Prophetic Prism");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBargainWithANontokenEnchantmentAndPaysBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(3);
        prepareCandyGrapple();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Up the Beanstalk");
        harness.assertInGraveyard(player1, "Up the Beanstalk");
        assertThat(target.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        harness.assertOnBattlefield(player2, "Edgewall Pack");
    }

    @Test
    void canBargainWithANonartifactNonenchantmentCreatureToken() {
        harness.castFromHand(player1, new EdgewallPack(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(3);
        prepareCandyGrapple();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(token.getId()));
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void cannotBargainWithANontokenNonartifactNonenchantmentCreature() {
        Permanent sacrifice = addCreatureReady(player1, new EdgewallPack());
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        prepareCandyGrapple();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Edgewall Pack");
        harness.assertInHand(player1, "Candy Grapple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBargainWithAnOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        prepareCandyGrapple();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertInHand(player1, "Candy Grapple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBargainWithoutSacrificingAPermanent() {
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        prepareCandyGrapple();

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Candy Grapple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAndSacrificeTheSameArtifactCreature() {
        Permanent target = addCreatureReady(player1, new Gingerbrute());
        prepareCandyGrapple();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), target.getId());
        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gingerbrute");
        harness.assertInGraveyard(player1, "Candy Grapple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void killsACreatureWithZeroToughnessWithoutBargain() {
        Permanent target = addCreatureReady(player2, new EdgewallPack());

        castCandyGrapple(target.getId());

        harness.assertNotOnBattlefield(player2, "Edgewall Pack");
        harness.assertInGraveyard(player2, "Edgewall Pack");
        harness.assertInGraveyard(player1, "Candy Grapple");
    }

    @Test
    void unbargainedReductionExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(5);
        castCandyGrapple(target.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Edgewall Pack");
    }

    @Test
    void bargainedReductionExpiresAtEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(5);
        prepareCandyGrapple();
        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Edgewall Pack");
    }

    @Test
    @CardUsed({VerazolTheSplitCurrent.class})
    void bargainingDoesNotTriggerVerazolsKickedSpellAbility() {
        harness.setHand(player1, List.of(new VerazolTheSplitCurrent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent target = addCreatureReady(player2, new EdgewallPack());
        target.setToughnessModifier(3);
        prepareCandyGrapple();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    private void prepareCandyGrapple() {
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castCandyGrapple(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
