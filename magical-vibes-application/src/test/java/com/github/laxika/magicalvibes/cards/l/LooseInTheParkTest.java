package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ExuberantWolfbear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MasterSymmetrist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LooseInThePark.class, ExuberantWolfbear.class, MasterSymmetrist.class, Forest.class})
class LooseInTheParkTest extends BaseCardTest {

    @Test
    void entersDrawsAndExilesAChosenSpellbookCardFaceUpWithTheAura() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LooseInThePark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftToExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftToExileChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        Permanent aura = findPermanent(player1, "Loose in the Park");
        assertThat(gd.getExiledWithPermanentEntries(aura.getId(), aura.getCard().getId()))
                .singleElement()
                .satisfies(entry -> assertThat(entry.faceDown()).isFalse());
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .first()
                .isInstanceOf(Forest.class);
    }

    @Test
    void activatesToMakeTheEnchantedLandACopyWhileKeepingItsLandType() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LooseInThePark());
        aura.setAttachedTo(forest.getId());
        Card drafted = new ExuberantWolfbear();
        gd.addToExile(player1.getId(), drafted, aura.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(forest.getCard().getName()).isEqualTo("Exuberant Wolfbear");
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }
}
