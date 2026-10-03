package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrierBreach.class, AngelicChorus.class, AuraOfSilence.class, GloriousAnthem.class, GrizzlyBears.class})
class BarrierBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three target enchantments")
    void exilesThreeTargetEnchantments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        castAndResolveBarrierBreach(List.of(first.getId(), second.getId(), third.getId()));

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Angelic Chorus"))
                .anyMatch(card -> card.getName().equals("Aura of Silence"));
    }

    @Test
    @DisplayName("Can choose fewer than three enchantments")
    void canChooseFewerEnchantments() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new AngelicChorus());

        castAndResolveBarrierBreach(List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Can choose no enchantments")
    void canChooseNoEnchantments() {
        harness.addToBattlefield(player2, new GloriousAnthem());

        castAndResolveBarrierBreach(List.of());

        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a nonenchantment permanent")
    void cannotTargetNonenchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Barrier Breach and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BarrierBreach()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Barrier Breach");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot choose more than three enchantments")
    void cannotChooseFourEnchantments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Barrier Breach");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose the same enchantment twice")
    void cannotChooseDuplicateTargets() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(enchantment.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Barrier Breach");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles remaining legal targets when other targets leave before resolution")
    void exilesRemainingLegalTargets() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        Permanent chorus = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();
        harness.castInstant(player1, 0, List.of(anthem.getId(), aura.getId(), chorus.getId()));

        harness.sacrificePermanent(player2, 0, anthem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Aura of Silence");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Angelic Chorus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Barrier Breach");
    }

    @Test
    @DisplayName("Does not resolve when all chosen targets leave the battlefield")
    void allTargetsBecomeIllegal() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();
        harness.castInstant(player1, 0, List.of(anthem.getId(), aura.getId()));

        harness.sacrificePermanent(player2, 0, anthem.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Aura of Silence");
        harness.assertInGraveyard(player1, "Barrier Breach");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling pays the discard cost before the draw resolves")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new BarrierBreach()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Barrier Breach");
        harness.assertNotInHand(player1, "Barrier Breach");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new BarrierBreach()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Barrier Breach");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveBarrierBreach(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
