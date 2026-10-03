package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveUpheaval.class, GrizzlyBears.class, HolyDay.class, Forest.class})
class GraveUpheavalTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureFromAnyGraveyardWithHaste() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GraveUpheaval()));
        addMana(4, ManaColor.BLACK, ManaColor.RED);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertInGraveyard(player1, "Grave Upheaval");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetNonCreatureCard() {
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player2, List.of(nonCreature));
        harness.setHand(player1, List.of(new GraveUpheaval()));
        addMana(4, ManaColor.BLACK, ManaColor.RED);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void basicLandcyclingSearchesForABasicLand() {
        harness.setHand(player1, List.of(new GraveUpheaval()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Upheaval");
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Forest");
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }
}
