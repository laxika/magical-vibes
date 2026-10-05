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
        harness.castFromHand(player1, new PorcinePortent(), "{3}{W}");
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

    @Test
    void chosenPigIsOwnedByTheConjuringPlayer() {
        harness.castFromHand(player1, new PorcinePortent(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice.cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("First Little Pig", "Second Little Pig", "Third Little Pig");
        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getOwnerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chosen);
    }

    @Test
    void conjuredFirstLittlePigCanExileAnEnchantment() {
        harness.castFromHand(player1, new PorcinePortent(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card chosen = choice.cards().stream()
                .filter(card -> card.getName().equals("First Little Pig"))
                .findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        Permanent enchantment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PorcinePortent)
                .findFirst().orElseThrow();
        Permanent pig = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == chosen)
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pig),
                null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.findExiledCard(enchantment.getCard().getId())).isNotNull();
    }

    @Test
    void adventureCountsBoarsAfterExilingYourOwnBoar() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new SecondLittlePig());
        harness.addToBattlefield(player2, new ThirdLittlePig());
        harness.setHand(player1, List.of(new PorcinePortent()));
        addAdventureMana();

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    @Test
    void adventureWithNoBoarsStillExilesAndCanBeCastAsAnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FirstLittlePig());
        PorcinePortent card = new PorcinePortent();
        harness.setHand(player1, List.of(card));
        addAdventureMana();
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();

        addPorcinePortentMana();
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.assertOnBattlefield(player1, "Porcine Portent");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void enchantmentDoesNotBoostOpposingBoars() {
        Permanent opponentBoar = harness.addToBattlefieldAndReturn(player2, new SecondLittlePig());
        int powerBefore = gqs.getEffectivePower(gd, opponentBoar);
        int toughnessBefore = gqs.getEffectiveToughness(gd, opponentBoar);
        harness.castFromHand(player1, new PorcinePortent(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        assertThat(gqs.getEffectivePower(gd, opponentBoar)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, opponentBoar)).isEqualTo(toughnessBefore);
    }

    @Test
    void adventureWithAnIllegalTargetDoesNotGainLifeOrGrantAnExilePermission() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SecondLittlePig());
        PorcinePortent first = new PorcinePortent();
        PorcinePortent second = new PorcinePortent();
        harness.setHand(player1, List.of(first, second));
        addAdventureMana();
        addAdventureMana();

        harness.castAdventure(player1, 0, target.getId());
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
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
