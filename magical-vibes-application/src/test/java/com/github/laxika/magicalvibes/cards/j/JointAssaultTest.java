package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.l.LightningMauler;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.cards.s.ShelteringWord;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
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

@CardUsed({JointAssault.class, WanderingWolf.class, LightningMauler.class, ScrollOfAvacyn.class,
        PeelFromReality.class, ShelteringWord.class})
class JointAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Unpaired target creature gets +2/+2")
    void boostsUnpairedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());

        castJointAssault(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paired target and its soulbond partner both get +2/+2")
    void boostsPairedPartnerToo() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");

        castJointAssault(wolf);

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both boosts wear off at end of turn")
    void boostsExpireAtEndOfTurn() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");

        castJointAssault(wolf);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrollOfAvacyn());
        harness.setHand(player1, List.of(new JointAssault()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's paired creatures")
    void boostsOpponentsPair() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");
        harness.setHand(player2, List.of(new JointAssault()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, wolf.getId());

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Targeting the soulbond creature also boosts its partner")
    void boostsPartnerWhenTargetingMauler() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");

        castJointAssault(mauler);

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Neither creature is boosted if the target leaves before resolution")
    void noBoostWhenTargetLeaves() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new JointAssault(), new PeelFromReality()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, wolf.getId());

        harness.castAndResolveInstant(player1, 0, List.of(wolf.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wandering Wolf");
        harness.assertInGraveyard(player1, "Joint Assault");
        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the target is boosted if its partner leaves before resolution")
    void boostsOnlyTargetWhenPartnerLeaves() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new JointAssault(), new PeelFromReality()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, wolf.getId());

        harness.castAndResolveInstant(player1, 0, List.of(mauler.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Mauler");
        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A surviving partner keeps its boost after the pair is broken")
    void boostPersistsAfterPartnerLeaves() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        castJointAssault(wolf);
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(wolf.getId(), opponent.getId()));

        assertThat(mauler.getPairedWithId()).isNull();
        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A hexproof partner still receives the bonus without being targeted")
    void boostsHexproofPartner() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");
        harness.setHand(player1, List.of(new ShelteringWord()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, mauler.getId());
        harness.setHand(player2, List.of(new JointAssault()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, wolf.getId());

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Neither creature is boosted if the target gains hexproof in response")
    void noBoostWhenTargetGainsHexproof() {
        Permanent wolf = pairMaulerWithWolf();
        Permanent mauler = findPermanent(player1, "Lightning Mauler");
        harness.setHand(player2, List.of(new JointAssault()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, wolf.getId());
        harness.setHand(player1, List.of(new ShelteringWord()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player2, "Joint Assault");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent pairMaulerWithWolf() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new LightningMauler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, wolf.getId());
        return wolf;
    }

    private void castJointAssault(Permanent target) {
        harness.setHand(player1, List.of(new JointAssault()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
