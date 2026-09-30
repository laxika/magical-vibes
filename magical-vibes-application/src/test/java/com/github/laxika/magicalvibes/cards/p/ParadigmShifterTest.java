package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DecorumDissertation;
import com.github.laxika.magicalvibes.cards.e.EchocastingSymposium;
import com.github.laxika.magicalvibes.cards.g.GerminationPracticum;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprovisationCapstone;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.RestorationSeminar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParadigmShifter.class, RestorationSeminar.class, EchocastingSymposium.class,
        DecorumDissertation.class, GerminationPracticum.class, ImprovisationCapstone.class,
        Opt.class, GrizzlyBears.class})
class ParadigmShifterTest extends BaseCardTest {

    @Test
    void enteringOffersParadigmSpellbookAndConjuresSelectedCard() {
        harness.enterBattlefieldAndReturn(player1, new ParadigmShifter());
        harness.passBothPriorities();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice.cards()).extracting(Card::getName).containsExactly(
                "Restoration Seminar",
                "Echocasting Symposium",
                "Decorum Dissertation",
                "Germination Practicum",
                "Improvisation Capstone");

        Card selected = choice.cards().stream()
                .filter(card -> card.getName().equals("Echocasting Symposium"))
                .findFirst()
                .orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        harness.assertInHand(player1, "Echocasting Symposium");
    }

    @Test
    void tapAddsIndependentlyChosenInstantSorceryOnlyMana() {
        addCreatureReady(player1, new ParadigmShifter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Opt()));
        harness.castInstant(player1, 0);
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }
}
