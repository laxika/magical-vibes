package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BladewheelChariot;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SpringLoadedSawblades.class, BladewheelChariot.class, CrawWurm.class,
        DarksteelRelic.class, GrizzlyBears.class})
class SpringLoadedSawbladesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and deals 5 damage to a tapped creature an opponent controls")
    void entersAndDamagesTappedOpponentCreature() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        target.tap();

        harness.setHand(player1, List.of(new SpringLoadedSawblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = addCreatureReady(player2, new CrawWurm());

        harness.setHand(player1, List.of(new SpringLoadedSawblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new CrawWurm());
        target.tap();

        harness.setHand(player1, List.of(new SpringLoadedSawblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns transformed")
    void craftsIntoBladewheelChariot() {
        Permanent sawblades = harness.addToBattlefieldAndReturn(player1, new SpringLoadedSawblades());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent chariot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BladewheelChariot)
                .findFirst().orElseThrow();
        assertThat(chariot.isTransformed()).isTrue();
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sawblades);
    }

    @Test
    @DisplayName("Two other artifacts animate the transformed Vehicle")
    void twoOtherArtifactsAnimateVehicle() {
        Permanent chariot = addTransformedChariot();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chariot)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chariot)).isEqualTo(5);
    }

    @Test
    @DisplayName("Crew 1 animates the transformed Vehicle")
    void crewAnimatesVehicle() {
        Permanent chariot = addTransformedChariot();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, chariot)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chariot)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chariot)).isEqualTo(5);
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    void untappedTargetIsNotDamagedOnResolution() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        target.tap();
        harness.setHand(player1, List.of(new SpringLoadedSawblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();

        target.untap();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertOnBattlefield(player1, "Spring-Loaded Sawblades");
    }

    @Test
    void canCastWithoutAnEligibleCreature() {
        harness.setHand(player1, List.of(new SpringLoadedSawblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spring-Loaded Sawblades");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftsWithAnArtifactCardFromGraveyard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SpringLoadedSawblades());
        SpringLoadedSawblades material = new SpringLoadedSawblades();
        harness.setGraveyard(player1, List.of(material));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.findExiledCard(source.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        Permanent chariot = findPermanent(player1, "Bladewheel Chariot");
        assertThat(chariot.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, chariot)).isFalse();
        assertThat(gd.findExiledCard(source.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
    }

    @Test
    void cannotCraftUsingOnlyItselfOrAnOpponentsArtifact() {
        harness.addToBattlefield(player1, new SpringLoadedSawblades());
        harness.addToBattlefield(player2, new SpringLoadedSawblades());
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Spring-Loaded Sawblades");
    }

    @Test
    void artifactTapCostCannotIncludeTheVehicle() {
        Permanent chariot = addTransformedChariot();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SpringLoadedSawblades());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chariot.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, chariot)).isFalse();
    }

    @Test
    void tappedArtifactsCannotPayTheAnimationCost() {
        Permanent chariot = addTransformedChariot();
        harness.addToBattlefield(player1, new SpringLoadedSawblades());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new SpringLoadedSawblades());
        tapped.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, chariot)).isFalse();
    }

    @Test
    void craftCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new SpringLoadedSawblades());
        harness.addToBattlefield(player1, new SpringLoadedSawblades());
        addCraftMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void flashAllowsCastingDuringCombat() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new SpringLoadedSawblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spring-Loaded Sawblades");
    }

    private Permanent addTransformedChariot() {
        SpringLoadedSawblades front = new SpringLoadedSawblades();
        Permanent chariot = new Permanent(front);
        chariot.setCard(front.getBackFaceCard());
        chariot.setTransformed(true);
        chariot.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(chariot);
        return chariot;
    }

}
