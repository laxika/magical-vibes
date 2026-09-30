package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LendAHam;
import com.github.laxika.magicalvibes.cards.s.SecondLittlePig;
import com.github.laxika.magicalvibes.cards.t.ThirdLittlePig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PorcinePortent.class, LendAHam.class, FirstLittlePig.class, SecondLittlePig.class,
        ThirdLittlePig.class, GrizzlyBears.class, Forest.class})
class PorcinePortentTest extends BaseCardTest {

    @Test
    void entersDraftsAChosenPigAndBoostsBoars() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new FirstLittlePig());
        Permanent nonBoar = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int boarPowerBefore = gqs.getEffectivePower(gd, boar);
        int boarToughnessBefore = gqs.getEffectiveToughness(gd, boar);
        int nonBoarPowerBefore = gqs.getEffectivePower(gd, nonBoar);
        PorcinePortent card = new PorcinePortent();
        harness.setHand(player1, List.of(card));
        addPorcinePortentMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card chosenPig = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosenPig.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(chosenPig);
        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(boarPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(boarToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, nonBoar)).isEqualTo(nonBoarPowerBefore);
    }

    @Test
    void adventureExilesTargetCreatureAndGainsLifeForEachBoar() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new SecondLittlePig());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PorcinePortent card = new PorcinePortent();
        harness.setHand(player1, List.of(card));
        addAdventureMana();

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetANonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PorcinePortent()));
        addAdventureMana();

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addPorcinePortentMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addAdventureMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
