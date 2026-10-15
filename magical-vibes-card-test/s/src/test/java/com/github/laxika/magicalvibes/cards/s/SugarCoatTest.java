package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SugarCoat.class, BakeIntoAPie.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, SimianSling.class})
class SugarCoatTest extends BaseCardTest {

    @Test
    @DisplayName("Turns an enchanted creature into a colorless Food artifact")
    void turnsCreatureIntoFoodArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castSugarCoat(target);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can enchant a Food and replace its ability")
    void enchantsFood() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent food = findPermanent(player1, "Food");
        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, food.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(gqs.isCreature(gd, food)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, food)).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature non-Food permanent")
    void rejectsNoncreatureNonFoodTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Food replaces the enchanted creature's Equipment subtype")
    void replacesOtherArtifactSubtypes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SimianSling());

        castSugarCoat(target);

        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.MONKEY)).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void canBeCastDuringOpponentsEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castSugarCoat(target);

        harness.assertOnBattlefield(player1, "Sugar Coat");
        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.FOOD)).isTrue();
    }

    @Test
    @DisplayName("A tapped enchanted permanent cannot pay the Food tap cost")
    void tappedPermanentCannotActivateFoodAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        target.tap();
        castSugarCoat(target);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Food ability requires two mana and replaces the printed mana ability")
    void cannotActivateWithoutTwoMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        castSugarCoat(target);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and Sugar Coat goes to its owner's graveyard")
    void sacrificeIsACostAndAuraGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        target.setSummoningSick(true);
        castSugarCoat(target);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Sugar Coat");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);
    }

    private void castSugarCoat(Permanent target) {
        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
