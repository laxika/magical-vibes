package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GnollHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonkClass.class, Forest.class, GnollHunter.class})
class MonkClassTest extends BaseCardTest {

    @Test
    void secondSpellEachTurnCostsOneLess() {
        harness.addToBattlefield(player1, new MonkClass());
        harness.setHand(player1, List.of(new GnollHunter(), new GnollHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof GnollHunter)
                .hasSize(2);
    }

    @Test
    void levelTwoReturnsUpToOneTargetNonlandPermanent() {
        Permanent monkClass = harness.addToBattlefieldAndReturn(player1, new MonkClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnollHunter());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        levelUpToTwo(monkClass);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(forest.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Gnoll Hunter");
    }

    @Test
    void levelThreeExilesTopCardAndAllowsCastingItAfterAnotherSpell() {
        Permanent monkClass = harness.addToBattlefieldAndReturn(player1, new MonkClass());
        GnollHunter exiledCard = new GnollHunter();
        levelUpToThree(monkClass);
        harness.setLibrary(player1, List.of(exiledCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCard);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GnollHunter()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof GnollHunter)
                .hasSize(2);
    }

    @Test
    void castingMonkClassFirstDiscountsTheNextSpell() {
        harness.setHand(player1, List.of(new MonkClass(), new GnollHunter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Monk Class");
        harness.assertOnBattlefield(player1, "Gnoll Hunter");
    }

    @Test
    void levelTwoCanDeclineToReturnAnyPermanent() {
        Permanent monkClass = harness.addToBattlefieldAndReturn(player1, new MonkClass());
        harness.addToBattlefield(player2, new GnollHunter());
        levelUpToTwo(monkClass);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(monkClass.getClassLevel()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Monk Class");
        harness.assertOnBattlefield(player2, "Gnoll Hunter");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void thirdSpellDoesNotReceiveTheDiscount() {
        harness.addToBattlefield(player1, new MonkClass());
        harness.setHand(player1, List.of(new GnollHunter(), new GnollHunter(), new GnollHunter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof GnollHunter).hasSize(3);
    }

    @Test
    void opponentsSecondSpellDoesNotReceiveTheDiscount() {
        harness.addToBattlefield(player1, new MonkClass());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GnollHunter(), new GnollHunter()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard() instanceof GnollHunter).hasSize(2);
    }

    @Test
    void cannotSkipLevelTwoOrLevelUpAtInstantSpeed() {
        Permanent monkClass = harness.addToBattlefieldAndReturn(player1, new MonkClass());
        prepareForSorcery();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(monkClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(monkClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(monkClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void levelTwoDoesNotExileCardsAtUpkeep() {
        Permanent monkClass = harness.addToBattlefieldAndReturn(player1, new MonkClass());
        levelUpToTwo(monkClass);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        GnollHunter topCard = new GnollHunter();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void exiledCardRemainsCastableAfterMonkClassLeaves() {
        Permanent monkClass = harness.addToBattlefieldAndReturn(player1, new MonkClass());
        levelUpToThree(monkClass);
        GnollHunter exiledCard = new GnollHunter();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, monkClass));
        harness.assertNotOnBattlefield(player1, "Monk Class");
        prepareForSorcery();
        harness.setHand(player1, List.of(new GnollHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof GnollHunter).hasSize(2);
    }

    private void levelUpToTwo(Permanent monkClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, battlefieldIndex(monkClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent monkClass) {
        levelUpToTwo(monkClass);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        prepareForSorcery();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(monkClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
