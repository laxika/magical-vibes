package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HonoredHeirloom;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SerpentineAmbush.class, HonoredHeirloom.class, SporeCrawler.class,
        TravelingMinister.class, SigardasSummons.class})
class SerpentineAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature a 5/5 blue Serpent")
    void transformsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());

        castSerpentineAmbush(target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.SERPENT);
    }

    @Test
    @DisplayName("All changes wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());
        var originalColors = gqs.getEffectiveColors(gd, target);
        var originalSubtypes = gqs.effectiveCreatureSubtypes(gd, target);

        castSerpentineAmbush(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).isEqualTo(originalSubtypes);
        assertThat(gqs.getEffectiveColors(gd, target)).isEqualTo(originalColors);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player2, new HonoredHeirloom());
        harness.addToBattlefield(player2, new SporeCrawler());
        harness.setHand(player1, List.of(new SerpentineAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID heirloomId = heirloom.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, heirloomId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counters remain on top of the new base power and toughness")
    void preservesCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castSerpentineAmbush(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("An existing boost still applies after setting base power and toughness")
    void preservesExistingBoost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent minister = addCreatureReady(player1, new TravelingMinister());
        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        castSerpentineAmbush(minister.getId());

        assertThat(gqs.getEffectivePower(gd, minister)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, minister)).isEqualTo(5);
    }

    @Test
    @DisplayName("A transformed creature retains its activated ability")
    void retainsActivatedAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent minister = addCreatureReady(player1, new TravelingMinister());
        int startingLife = gd.getLife(player1.getId());

        castSerpentineAmbush(minister.getId());
        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, minister)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, minister)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, minister)).containsExactly(CardSubtype.SERPENT);
    }

    @Test
    @DisplayName("Replaces creature types granted by an earlier Sigarda's Summons")
    void replacesEarlierGrantedType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castSigardasSummons();

        castSerpentineAmbush(target.getId());

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.SERPENT);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("A later Sigarda's Summons adds Angel to the Serpent creature type")
    void allowsLaterGrantedType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castSerpentineAmbush(target.getId());

        castSigardasSummons();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.SERPENT, CardSubtype.ANGEL);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    private void castSigardasSummons() {
        harness.setHand(player1, List.of(new SigardasSummons()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void castSerpentineAmbush(UUID targetId) {
        harness.setHand(player1, List.of(new SerpentineAmbush()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
