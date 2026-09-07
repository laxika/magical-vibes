package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DreadWhispers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MosswoodDreadknight.class, DreadWhispers.class, Forest.class})
class MosswoodDreadknightTest extends BaseCardTest {

    @Test
    void adventureDrawsACardAndLosesLife() {
        Forest draw = new Forest();
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void deathMayCastAdventureFromGraveyard() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
