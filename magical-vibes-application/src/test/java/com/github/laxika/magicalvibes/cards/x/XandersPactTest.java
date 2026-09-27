package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XandersPact.class, Shock.class, Forest.class, GrizzlyBears.class})
class XandersPactTest extends BaseCardTest {

    @Test
    void exilesEachOpponentsTopCardAndGrantsLifeCastPermissionToNonlands() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(shock, forest));
        castPact();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
        assertThat(gd.exilePlayForLifeEqualToManaValue).contains(shock.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(forest.getId());
    }

    @Test
    void castsAnExiledSpellByPayingLifeEqualToManaValue() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        castPact();

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(bears));
    }

    @Test
    void casualtyCopiesTheExileEffect() {
        Shock first = new Shock();
        Shock second = new Shock();
        Permanent casualtyCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new XandersPact()));
        addPactMana();

        harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    private void castPact() {
        harness.setHand(player1, List.of(new XandersPact()));
        addPactMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void addPactMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
