package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.m.Miscalculation;
import com.github.laxika.magicalvibes.cards.t.ThranLens;
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

@CardUsed({OpalChampion.class, AngelicCurator.class, Miscalculation.class, ThranLens.class})
class OpalChampionTest extends BaseCardTest {

    private Permanent addOpalChampion() {
        return harness.addToBattlefieldAndReturn(player1, new OpalChampion());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentCreature() {
        harness.castFromHand(player2, new AngelicCurator(), "{1}{W}");
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Champion a 3/3 Knight creature with first strike")
    void becomesKnightCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalChampion();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.KNIGHT);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Champion has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalChampion();
        prepareOpponentCast();

        castOpponentCreature();
        resolveAllTriggers();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Champion")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalChampion();
        prepareOpponentCast();

        harness.castFromHand(player2, new ThranLens(), "{2}");
        assertThat(gd.stack).hasSize(1);

        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalChampion();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new AngelicCurator(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    void transformsWhenOpponentCreatureSpellIsCountered() {
        Permanent opal = addOpalChampion();
        prepareOpponentCast();

        AngelicCurator creature = new AngelicCurator();
        harness.castFromHand(player2, creature, "{1}{W}");
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Miscalculation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        harness.assertInGraveyard(player2, creature.getName());
    }
}
