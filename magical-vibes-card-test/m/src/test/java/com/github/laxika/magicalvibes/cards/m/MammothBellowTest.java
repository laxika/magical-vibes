package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MammothBellow.class, HillGiant.class})
class MammothBellowTest extends BaseCardTest {

    @Test
    void normalCastCreatesFiveFiveGreenElephant() {
        harness.setHand(player1, List.of(new MammothBellow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(elephantTokens()).hasSize(1);
        Permanent elephant = elephantTokens().getFirst();
        assertThat(elephant.getCard().getPower()).isEqualTo(5);
        assertThat(elephant.getCard().getToughness()).isEqualTo(5);
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    void harmonizeCreatesElephantAndExilesSpellWhileTappingCreatureForReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        MammothBellow spell = new MammothBellow();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(elephantTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Mammoth Bellow");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeWithoutTappingPaysFullCostAndExilesSpell() {
        MammothBellow spell = new MammothBellow();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(elephantTokens()).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Mammoth Bellow");
    }

    @Test
    void newlyCreatedElephantCanPayEntireGenericHarmonizeCost() {
        harness.setHand(player1, List.of(new MammothBellow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent elephant = elephantTokens().getFirst();
        assertThat(elephant.isSummoningSick()).isTrue();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashbackWithTapCost(player1, 0, List.of(elephant.getId()));
        harness.passBothPriorities();

        assertThat(elephant.isTapped()).isTrue();
        assertThat(elephantTokens()).hasSize(2);
        harness.assertNotInGraveyard(player1, "Mammoth Bellow");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void harmonizeCannotTapAnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        creature.tap();
        MammothBellow spell = new MammothBellow();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Mammoth Bellow");
        assertThat(elephantTokens()).isEmpty();
    }

    private List<Permanent> elephantTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elephant"))
                .toList();
    }
}
