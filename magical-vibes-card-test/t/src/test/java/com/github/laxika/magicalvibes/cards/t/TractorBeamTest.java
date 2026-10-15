package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuraFinesse;
import com.github.laxika.magicalvibes.cards.e.EumidianTerrabotanist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SpecimenFreighter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TractorBeam.class, EumidianTerrabotanist.class, SpecimenFreighter.class, Forest.class, AuraFinesse.class})
class TractorBeamTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and gains control of the enchanted creature")
    void tapsAndControlsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new EumidianTerrabotanist());

        castAndResolve(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can enchant, tap, and control a Spacecraft")
    void controlsSpacecraft() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player2, new SpecimenFreighter());

        castAndResolve(spacecraft);

        assertThat(spacecraft.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spacecraft);
    }

    @Test
    @DisplayName("The enchanted permanent does not untap during its controller's untap step")
    void enchantedPermanentDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new EumidianTerrabotanist());

        castAndResolve(creature);
        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a permanent that is neither a creature nor a Spacecraft")
    void cannotEnchantOtherPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or Spacecraft");
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new TractorBeam()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    void controlChangesBeforeTapTriggerResolves() {
        Permanent creature = addCreatureReady(player2, new EumidianTerrabotanist());
        prepareCast();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(creature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void enchantedSpacecraftDoesNotUntap() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player2, new SpecimenFreighter());
        Permanent otherCreature = addCreatureReady(player1, new EumidianTerrabotanist());
        otherCreature.tap();
        castAndResolve(spacecraft);

        harness.performUntapStep(player1);

        assertThat(spacecraft.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    void removingAuraReturnsControlAndAllowsUntapping() {
        Permanent creature = addCreatureReady(player2, new EumidianTerrabotanist());
        castAndResolve(creature);
        Permanent aura = findPermanent(player1, "Tractor Beam");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(creature.isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @CardUsed({AuraFinesse.class})
    void tapTriggerUsesCurrentEnchantedPermanentAfterAuraMoves() {
        Permanent original = addCreatureReady(player2, new EumidianTerrabotanist());
        Permanent destination = addCreatureReady(player2, new EumidianTerrabotanist());
        prepareCast();
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Tractor Beam");

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, List.of(aura.getId(), destination.getId()));

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(original.isTapped()).isFalse();
        assertThat(destination.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(destination.isTapped()).isTrue();
        assertThat(original.isTapped()).isFalse();
    }
}
