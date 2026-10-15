package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.cards.o.OrazcaFrillback;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoraciousVampire.class, DuskLegionZealot.class, OrazcaFrillback.class, MomentOfCraving.class})
class VoraciousVampireTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a Vampire you control +1/+1 and menace")
    void etbBoostsVampireAndGrantsMenace() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID vampireId = harness.getPermanentId(player1, "Dusk Legion Zealot");
        harness.castCreature(player1, 0, List.of(vampireId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = findPermanent(player1, "Dusk Legion Zealot");
        assertThat(vampire.getPowerModifier()).isEqualTo(1);
        assertThat(vampire.getToughnessModifier()).isEqualTo(1);
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("ETB boost and menace wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID vampireId = harness.getPermanentId(player1, "Dusk Legion Zealot");
        harness.castCreature(player1, 0, List.of(vampireId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = findPermanent(player1, "Dusk Legion Zealot");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isZero();
        assertThat(vampire.getToughnessModifier()).isZero();
        assertThat(vampire.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Rejects a non-Vampire target")
    void rejectsNonVampireTarget() {
        harness.addToBattlefield(player1, new OrazcaFrillback());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID dinosaurId = harness.getPermanentId(player1, "Orazca Frillback");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Voracious Vampire");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, dinosaurId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects an opponent's Vampire target")
    void rejectsOpponentsVampireTarget() {
        harness.addToBattlefield(player2, new DuskLegionZealot());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID vampireId = harness.getPermanentId(player2, "Dusk Legion Zealot");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Voracious Vampire");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, vampireId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Dusk Legion Zealot").getPowerModifier()).isZero();
        assertThat(source.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void canEnterOnAnEmptyBattlefieldAndTargetItself() {
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Voracious Vampire");
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new VoraciousVampire(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Voracious Vampire");

        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertInGraveyard(player1, "Voracious Vampire");
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetDoesNotRedirectBoostToSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DuskLegionZealot());
        harness.setHand(player1, List.of(new VoraciousVampire(), new MomentOfCraving()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Voracious Vampire");

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player1, "Dusk Legion Zealot");
        resolveAllTriggers();

        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
