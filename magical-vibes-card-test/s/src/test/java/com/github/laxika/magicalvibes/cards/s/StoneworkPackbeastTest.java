package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneworkPackbeast.class})
class StoneworkPackbeastTest extends BaseCardTest {

    @Test
    void isAlsoEachPartyCreatureType() {
        Permanent packbeast = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());

        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.CLERIC)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.ROGUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.WARRIOR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, packbeast, CardSubtype.WIZARD)).isTrue();
    }

    @Test
    void paysTwoManaForOneManaOfAnyColorWithoutTapping() {
        Permanent packbeast = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(packbeast.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hasPartyTypesOutsideTheBattlefield() {
        StoneworkPackbeast inHand = new StoneworkPackbeast();
        StoneworkPackbeast inLibrary = new StoneworkPackbeast();
        StoneworkPackbeast inGraveyard = new StoneworkPackbeast();
        StoneworkPackbeast inExile = new StoneworkPackbeast();
        harness.setHand(player1, List.of(inHand));
        harness.setLibrary(player1, List.of(inLibrary));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.setExile(player1, List.of(inExile));

        for (StoneworkPackbeast card : List.of(inHand, inLibrary, inGraveyard, inExile)) {
            assertThat(gqs.getCardSubtypes(card, gd, player1.getId()))
                    .contains(CardSubtype.CLERIC, CardSubtype.ROGUE,
                            CardSubtype.WARRIOR, CardSubtype.WIZARD);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canProduceEachManaColor(ManaColor color) {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent packbeast = harness.addToBattlefieldAndReturn(player1, new StoneworkPackbeast());
        packbeast.tap();
        packbeast.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(packbeast.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
