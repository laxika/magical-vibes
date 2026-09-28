package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZenithFestival.class, Forest.class, GrizzlyBears.class})
class ZenithFestivalTest extends BaseCardTest {

    @Test
    void exilesTopXCardsAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.setHand(player1, List.of(new ZenithFestival()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void harmonizeCastsWithXAndCreaturePowerReduction() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ZenithFestival spell = new ZenithFestival();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playFlashbackSpell(gd, player1, 0, 2, null, List.of(), List.of(), null,
                List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }
}
