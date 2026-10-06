package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Fly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({RayOfFrost.class, FountainOfYouth.class, GrizzlyBears.class, ProdigalPyromancer.class,
        Fly.class, RobeOfMirrors.class})
class RayOfFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Ray of Frost taps a red creature when it enters")
    void tapsRedCreatureOnEnter() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        castRayOfFrost(pyromancer);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ray of Frost does not tap a nonred creature when it enters")
    void doesNotTapNonredCreatureOnEnter() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castRayOfFrost(bears);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ray of Frost removes a red creature's activated abilities")
    void redCreatureLosesAbilities() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        attachRayOfFrost(pyromancer);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ray of Frost keeps the enchanted creature tapped through its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        pyromancer.tap();
        attachRayOfFrost(pyromancer);

        harness.performUntapStep(player2);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ray of Frost cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RayOfFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void nonredCreatureAlsoDoesNotUntapButOtherCreaturesDo() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        enchanted.tap();
        other.tap();
        castRayOfFrost(enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    void canBeCastDuringOpponentsCombat() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        castRayOfFrost(bears);

        assertThat(findPermanent(player1, "Ray of Frost").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void canEnchantControllersOwnRedCreature() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        castRayOfFrost(pyromancer);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    void redCreatureKeepsAbilitiesGrantedAfterRayOfFrost() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        castRayOfFrost(pyromancer);

        castFly(pyromancer);

        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.FLYING)).isTrue();
    }

    @Test
    void redCreatureLosesAbilitiesGrantedBeforeRayOfFrost() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        castFly(pyromancer);

        castRayOfFrost(pyromancer);

        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.FLYING)).isFalse();
    }

    @Test
    void nonredCreatureKeepsItsGrantedAbilities() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castFly(bears);

        castRayOfFrost(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void enterTriggerDoesNotTargetEnchantedCreature() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new RayOfFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, pyromancer.getId());
        harness.passBothPriorities();
        assertThat(pyromancer.isTapped()).isFalse();

        Permanent robe = harness.addToBattlefieldAndReturn(player2, new RobeOfMirrors());
        robe.setAttachedTo(pyromancer.getId());
        robe.setTimestamp(gd.nextTimestamp());
        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.SHROUD)).isTrue();

        resolveAllTriggers();

        assertThat(pyromancer.isTapped()).isTrue();
    }

    private void castFly(Permanent target) {
        harness.setHand(player1, List.of(new Fly()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void castRayOfFrost(Permanent target) {
        harness.setHand(player1, List.of(new RayOfFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void attachRayOfFrost(Permanent target) {
        Permanent aura = new Permanent(new RayOfFrost());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
