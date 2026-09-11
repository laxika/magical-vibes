package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnchorToReality.class, BoneSaw.class, Forest.class, GoldMyr.class, HillGiant.class,
        LeoninScimitar.class, SolRing.class})
class AnchorToRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, finds a lower-mana-value Equipment, and scries 2")
    void findsLowerManaValueEquipmentAndScries() {
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        AnchorToReality spell = new AnchorToReality();
        BoneSaw found = new BoneSaw();
        Card firstScryCard = new Forest();
        Card secondScryCard = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(found, firstScryCard, secondScryCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(found);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactlyInAnyOrder(firstScryCard, secondScryCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepts an artifact sacrifice but does not scry for an equal-mana-value Equipment")
    void equalManaValueDoesNotScry() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SolRing());
        AnchorToReality spell = new AnchorToReality();
        LeoninScimitar found = new LeoninScimitar();
        GoldMyr ineligible = new GoldMyr();
        Card land = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(found, ineligible, land));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Sol Ring");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(found);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(ineligible, land);
        assertThat(gd.stack).isEmpty();
    }
}
