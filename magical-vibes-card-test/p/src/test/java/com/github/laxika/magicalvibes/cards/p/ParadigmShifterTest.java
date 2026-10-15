package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DecorumDissertation;
import com.github.laxika.magicalvibes.cards.e.EchocastingSymposium;
import com.github.laxika.magicalvibes.cards.f.FalseDawn;
import com.github.laxika.magicalvibes.cards.g.GerminationPracticum;
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
        Opt.class})
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

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new ParadigmShifter()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        gd.playerManaPools.get(player1.getId()).clear();
        findPermanent(player1, "Paradigm Shifter").untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.setHand(player1, List.of(new Opt()));
        harness.castInstant(player1, 0);
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void sameColorManaPaysColoredSorceryCostWithoutUsingTheStack() {
        var shifter = addCreatureReady(player1, new ParadigmShifter());
        harness.setHand(player1, List.of(new GerminationPracticum()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(shifter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getInstantSorceryOnlyColored(ManaColor.GREEN)).isEqualTo(2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(shifter.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getInstantSorceryOnlyColored(ManaColor.GREEN)).isZero();
    }

    @Test
    void summoningSickCreatureCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new ParadigmShifter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({FalseDawn.class})
    void falseDawnReplacesBothChosenColorsWithRestrictedWhiteMana() {
        addCreatureReady(player1, new ParadigmShifter());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.setLibrary(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.WHITE)).isEqualTo(2);
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.GREEN)).isZero();
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.RED)).isZero();
    }

    @Test
    void triggerConjuresForItsControllerAfterSourceLeaves() {
        var shifter = harness.enterBattlefieldAndReturn(player2, new ParadigmShifter());
        gd.playerBattlefields.get(player2.getId()).remove(shifter);
        gd.playerGraveyards.get(player2.getId()).add(shifter.getCard());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        Card selected = choice.cards().getFirst();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(selected.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player2, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player2.getId())).contains(selected);
        assertThat(selected.getOwnerId()).isEqualTo(player2.getId());
        harness.assertNotInHand(player1, selected.getName());
    }
}
