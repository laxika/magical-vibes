package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.cards.m.MagistratesScepter;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.OrazcaRelic;
import com.github.laxika.magicalvibes.cards.p.PyreOfHeroes;
import com.github.laxika.magicalvibes.cards.r.RaidersKarve;
import com.github.laxika.magicalvibes.cards.r.ReplicatingRing;
import com.github.laxika.magicalvibes.cards.r.RelicAmulet;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpikedPitTrap;
import com.github.laxika.magicalvibes.cards.t.TreasureChest;
import com.github.laxika.magicalvibes.cards.w.WeaponRack;
import com.github.laxika.magicalvibes.cards.w.Whirlermaker;
import com.github.laxika.magicalvibes.cards.c.ColossalPlow;
import com.github.laxika.magicalvibes.cards.f.FiftyFeetOfRope;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakExpectations.class, ColossalPlow.class, Millstone.class, Whirlermaker.class,
        MagistratesScepter.class, ReplicatingRing.class, RaidersKarve.class, WeaponRack.class,
        RelicAmulet.class, OrazcaRelic.class, FiftyFeetOfRope.class, PyreOfHeroes.class,
        TreasureChest.class, LeatherArmor.class, SpikedPitTrap.class, Gingerbrute.class,
        GrizzlyBears.class, HillGiant.class, Shock.class})
class BreakExpectationsTest extends BaseCardTest {

    @Test
    void revealsOnlyQualifyingCardsExilesTheChosenCardAndLetsTargetDraftAndReveal() {
        Card lowManaValue = new Shock();
        Card firstQualifyingCard = new GrizzlyBears();
        Card secondQualifyingCard = new HillGiant();
        harness.setHand(player1, List.of(new BreakExpectations()));
        harness.setHand(player2, List.of(lowManaValue, firstQualifyingCard, secondQualifyingCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedMatchingHandCardChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedMatchingHandCardChoice.class);
        assertThat(handChoice).isNotNull();
        assertThat(handChoice.cards()).containsExactly(firstQualifyingCard, secondQualifyingCard);
        assertThat(handChoice.cards()).doesNotContain(lowManaValue);

        harness.handleMultipleCardsChosen(player1, List.of(firstQualifyingCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstQualifyingCard);
        PendingInteraction.SpellbookDraftChoice draft =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(draft).isNotNull();
        assertThat(draft.playerId()).isEqualTo(player2.getId());
        assertThat(draft.revealChosenCard()).isTrue();

        Card drafted = draft.cards().getFirst();
        harness.handleMultipleCardsChosen(player2, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player2.getId())).contains(drafted, lowManaValue, secondQualifyingCard);
    }

    @Test
    void doesNotDraftWhenNoCardHasManaValueAtLeastTwo() {
        Card lowManaValue = new Shock();
        harness.setHand(player1, List.of(new BreakExpectations()));
        harness.setHand(player2, List.of(lowManaValue));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(lowManaValue);
    }
}
