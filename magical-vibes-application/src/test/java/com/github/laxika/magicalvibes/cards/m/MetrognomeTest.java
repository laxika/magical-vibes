package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CacklingFiend;
import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Metrognome.class, Duress.class, Catalog.class, Forest.class, CacklingFiend.class})
class MetrognomeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four Gnome artifact creature tokens when discarded by an opponent")
    void createsFourTokensWhenDiscardedByOpponent() {
        harness.setHand(player2, List.of(new Metrognome()));
        harness.setHand(player1, List.of(new Duress()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> gnomes = findPermanents(player2, "Gnome");
        assertThat(gnomes).hasSize(4);
        assertThat(gnomes).allSatisfy(gnome -> {
            assertThat(gnome.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(1);
            assertThat(gqs.isArtifact(gd, gnome)).isTrue();
            assertThat(gqs.isCreature(gd, gnome)).isTrue();
            assertThat(gnome.getCard().getSubtypes()).contains(CardSubtype.GNOME);
            assertThat(gqs.getEffectiveColors(gd, gnome)).isEmpty();
        });
    }

    @Test
    @DisplayName("Creates four Gnome artifact creature tokens when an opponent's ability causes the discard")
    void createsFourTokensWhenDiscardedByOpponentAbility() {
        harness.setHand(player2, List.of(new Metrognome()));
        harness.castFromHand(player1, new CacklingFiend(), "{2}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        List<Permanent> gnomes = findPermanents(player2, "Gnome");
        assertThat(gnomes).hasSize(4);
        assertThat(gnomes).allSatisfy(gnome -> {
            assertThat(gnome.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(1);
            assertThat(gqs.isArtifact(gd, gnome)).isTrue();
            assertThat(gqs.isCreature(gd, gnome)).isTrue();
            assertThat(gnome.getCard().getSubtypes()).contains(CardSubtype.GNOME);
            assertThat(gqs.getEffectiveColors(gd, gnome)).isEmpty();
        });
    }

    @Test
    @DisplayName("Does not trigger when discarded by its controller")
    void doesNotTriggerWhenDiscardedByController() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Catalog(), new Metrognome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("Gnome"));
    }

    @Test
    @DisplayName("{4}, {T} creates one Gnome artifact creature token")
    void activatedAbilityCreatesOneToken() {
        Permanent metrognome = harness.addToBattlefieldAndReturn(player1, new Metrognome());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(metrognome.isTapped()).isTrue();
        harness.passBothPriorities();

        List<Permanent> gnomes = findPermanents(player1, "Gnome");
        assertThat(gnomes).hasSize(1);
        assertThat(gnomes.getFirst().getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, gnomes.getFirst())).isTrue();
        assertThat(gqs.isCreature(gd, gnomes.getFirst())).isTrue();
    }
}
