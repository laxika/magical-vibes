package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetrognomeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four Gnome artifact creature tokens when discarded by an opponent")
    void createsFourTokensWhenDiscardedByOpponent() {
        harness.setHand(player2, new ArrayList<>(List.of(new Metrognome())));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> gnomes = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Gnome"))
                .toList();
        assertThat(gnomes).hasSize(4);
        assertThat(gnomes).allSatisfy(gnome -> {
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
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(),
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears()));
        harness.setHand(player1, List.of(new Sift(), new Metrognome()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("Gnome"));
    }

    @Test
    @DisplayName("{4}, {T} creates one Gnome artifact creature token")
    void activatedAbilityCreatesOneToken() {
        harness.addToBattlefield(player1, new Metrognome());
        Permanent metrognome = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(metrognome.isTapped()).isTrue();
        harness.passBothPriorities();

        List<Permanent> gnomes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Gnome"))
                .toList();
        assertThat(gnomes).hasSize(1);
        assertThat(gqs.isArtifact(gd, gnomes.getFirst())).isTrue();
        assertThat(gqs.isCreature(gd, gnomes.getFirst())).isTrue();
    }
}
