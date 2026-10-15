package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
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

@DisplayName("Storm Elemental")
@CardUsed({
        StormElemental.class, WindSpirit.class, BalduvianBears.class, Island.class, SnowCoveredIsland.class,
        PullFromEternity.class
})
class StormElementalTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps target creature with flying and exiles the top library card")
    void tapsFlyingTarget() {
        addCreatureReady(player1, new StormElemental());
        Permanent flyer = addCreatureReady(player2, new WindSpirit());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("First ability cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        addCreatureReady(player1, new StormElemental());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability pumps when the exiled card is a snow land")
    void pumpsOnSnowLand() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Second ability does not pump when the exiled card is a nonsnow land")
    void doesNotPumpOnNonsnowLand() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second ability's boost wears off at end of turn")
    void boostWearsOff() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        gd.playerDecks.get(player1.getId()).addFirst(new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Neither ability can be activated with an empty library")
    void cannotActivateWithEmptyLibrary() {
        addCreatureReady(player1, new StormElemental());
        Permanent flyer = addCreatureReady(player2, new WindSpirit());
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    @Test
    @DisplayName("Each activation checks the card exiled for that activation")
    void eachActivationUsesItsOwnExiledCard() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Second ability uses the snow land's last known information after it leaves exile")
    void pumpsAfterExiledSnowLandMovesToGraveyard() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        SnowCoveredIsland snowLand = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(snowLand));
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.findExiledCard(snowLand.getId())).isNotNull();
        harness.castInstant(player1, 0, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(snowLand.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snowLand);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple snow land activations give cumulative boosts")
    void snowLandBoostsAccumulate() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(6);
    }

    @Test
    @DisplayName("Second ability does not pump for an exiled creature")
    void doesNotPumpOnCreature() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        harness.setLibrary(player1, List.of(new BalduvianBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent elemental = addCreatureReady(player1, new StormElemental());
        elemental.setSummoningSick(true);
        elemental.tap();
        Permanent flyer = addCreatureReady(player2, new WindSpirit());
        harness.setLibrary(player1, List.of(new Island(), new SnowCoveredIsland()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
    }
}
