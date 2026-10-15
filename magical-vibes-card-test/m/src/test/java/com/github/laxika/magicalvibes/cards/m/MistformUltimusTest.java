package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.l.LordOfAtlantis;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistformUltimus.class, LordOfAtlantis.class, Conspiracy.class})
class MistformUltimusTest extends BaseCardTest {

    @Test
    @DisplayName("Mistform Ultimus is every creature type on the battlefield")
    void isEveryCreatureTypeOnBattlefield() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        Permanent mistform = harness.addToBattlefieldAndReturn(player1, new MistformUltimus());

        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.MERFOLK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mistform)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mistform)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mistform Ultimus is every creature type while it is in hand")
    void isEveryCreatureTypeOutsideBattlefield() {
        MistformUltimus mistform = new MistformUltimus();
        harness.setHand(player1, List.of(mistform));

        assertThat(gqs.cardHasSubtype(mistform, CardSubtype.ELF, gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Mistform Ultimus is every creature type in every non-battlefield zone")
    void isEveryCreatureTypeInEveryNonBattlefieldZone() {
        MistformUltimus handMistform = new MistformUltimus();
        MistformUltimus graveyardMistform = new MistformUltimus();
        MistformUltimus libraryMistform = new MistformUltimus();
        MistformUltimus exileMistform = new MistformUltimus();

        harness.setHand(player1, List.of(handMistform));
        harness.setGraveyard(player1, List.of(graveyardMistform));
        harness.setLibrary(player1, List.of(libraryMistform));
        harness.setExile(player1, List.of(exileMistform));

        assertThat(gqs.cardHasSubtype(handMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(libraryMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(exileMistform, CardSubtype.WALL, gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Mistform Ultimus is every creature type while it is a creature spell on the stack")
    void isEveryCreatureTypeOnStack() {
        harness.castFromHand(player1, new MistformUltimus(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.cardHasSubtype(gd.stack.getFirst().getCard(), CardSubtype.MERFOLK, gd, player1.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("Conspiracy replaces all creature types when Mistform Ultimus enters later")
    void conspiracyOverridesTypesWhenMistformEntersLater() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.castFromHand(player1, new MistformUltimus(), "{3}{U}");
        harness.passBothPriorities();
        Permanent mistform = findPermanent(player1, "Mistform Ultimus");

        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.MERFOLK)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.ILLUSION)).isFalse();
    }

    @Test
    @DisplayName("Conspiracy replaces all creature types when Mistform Ultimus enters first")
    void conspiracyOverridesTypesWhenMistformEntersFirst() {
        Permanent mistform = harness.addToBattlefieldAndReturn(player1, new MistformUltimus());

        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.MERFOLK)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, mistform, CardSubtype.ILLUSION)).isFalse();
    }

    @Test
    @DisplayName("Mistform Ultimus does not grant creature types to other creatures")
    void doesNotGrantTypesToOtherCreatures() {
        harness.addToBattlefield(player1, new MistformUltimus());
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LordOfAtlantis());

        assertThat(gqs.hasEffectiveSubtype(gd, lord, CardSubtype.MERFOLK)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, lord, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
    }
}
