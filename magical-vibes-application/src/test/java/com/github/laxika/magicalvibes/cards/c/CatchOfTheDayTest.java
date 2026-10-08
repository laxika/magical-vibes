package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatchOfTheDay.class, Clone.class, Unsummon.class})
class CatchOfTheDayTest extends BaseCardTest {

    @Test
    void choosesOneModeFromEachIndependentGroup() {
        harness.castFromHand(player1, new CatchOfTheDay(), "{5}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");
        harness.handleListChoice(player1, "Scry 2");
        harness.handleListChoice(player1, "4/4");

        Permanent catchOfTheDay = findPermanent(player1, "Catch of the Day");
        assertThat(catchOfTheDay.getChosenModeLabels()).containsExactlyInAnyOrder("Vigilance", "Scry 2", "4/4");
        assertThat(gqs.getEffectivePower(gd, catchOfTheDay)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, catchOfTheDay)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, catchOfTheDay, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void vigilanceAttackScriesTwoWithoutAnOpposingCreature() {
        Permanent serpent = enter(player1, "Vigilance", "Scry 2", "6/2");
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(2);
        CatchOfTheDay first = new CatchOfTheDay();
        CatchOfTheDay second = new CatchOfTheDay();
        harness.setLibrary(player1, List.of(first, second, new CatchOfTheDay()));

        declareAttackers(List.of(0));
        assertThat(serpent.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(second);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(first);
    }

    @Test
    void attackTapsOnlyTheChosenOpposingCreature() {
        Permanent target = enter(player2, "Vigilance", "Scry 2", "4/4");
        Permanent other = enter(player2, "Vigilance", "Scry 2", "4/4");
        Permanent serpent = enter(player1, "Islandwalk", "Tap target creature an opponent controls", "2/6");
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(6);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void attackGoadsOnlyTheChosenOpposingCreature() {
        Permanent target = enter(player2, "Vigilance", "Scry 2", "4/4");
        enter(player2, "Vigilance", "Scry 2", "4/4");
        enter(player1, "Vigilance", "Goad target creature an opponent controls", "4/4");

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("must attack");
    }

    @Test
    void wardCountersAnOpponentsSpellWithoutThreeMana() {
        Permanent serpent = enter(player1, "Ward {3}", "Scry 2", "4/4");
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.WARD)).isTrue();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, serpent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Unsummon");
        harness.assertOnBattlefield(player1, "Catch of the Day");
    }

    @Test
    void payingThreeManaAllowsOpponentsSpellToResolve() {
        harness.setHand(player1, List.of());
        Permanent serpent = enter(player1, "Ward {3}", "Scry 2", "4/4");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, serpent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void choosingVigilanceDoesNotGrantWardAndReentryAllowsNewChoices() {
        harness.setHand(player1, List.of());
        Permanent serpent = enter(player1, "Vigilance", "Scry 2", "6/2");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, serpent.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Islandwalk");
        harness.handleListChoice(player1, "Tap target creature an opponent controls");
        harness.handleListChoice(player1, "2/6");

        Permanent returned = findPermanent(player1, "Catch of the Day");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(6);
    }

    @Test
    void enteringCopyRetainsOriginalKeywordAndAddsNewKeyword() {
        Permanent original = enter(player1, "Vigilance", "Scry 2", "6/2");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        harness.handleListChoice(player1, "Ward {3}");
        harness.handleListChoice(player1, "Tap target creature an opponent controls");
        harness.handleListChoice(player1, "2/6");

        Permanent copy = findPermanents(player1, "Catch of the Day").stream()
                .filter(permanent -> permanent != original).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.WARD)).isTrue();
    }

    private Permanent enter(Player player, String keyword, String attack, String size) {
        Permanent permanent = harness.enterBattlefieldAndReturn(player, new CatchOfTheDay());
        harness.handleListChoice(player, keyword);
        harness.handleListChoice(player, attack);
        harness.handleListChoice(player, size);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
