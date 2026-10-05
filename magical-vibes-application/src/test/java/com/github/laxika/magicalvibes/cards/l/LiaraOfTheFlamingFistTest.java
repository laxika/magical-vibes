package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiaraOfTheFlamingFist.class, SteadfastPaladin.class})
class LiaraOfTheFlamingFistTest extends BaseCardTest {

    @Test
    void boostsCreaturesSharingANameAtBeginningOfEachCombat() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent firstPaladin = addCreatureReady(player1, new SteadfastPaladin());
        Permanent secondPaladin = addCreatureReady(player1, new SteadfastPaladin());
        Permanent opposingPaladin = addCreatureReady(player2, new SteadfastPaladin());

        advanceToCombat(player1);

        assertThat(firstPaladin.getPowerModifier()).isEqualTo(1);
        assertThat(firstPaladin.getToughnessModifier()).isEqualTo(1);
        assertThat(secondPaladin.getPowerModifier()).isEqualTo(1);
        assertThat(liara.getPowerModifier()).isZero();
        assertThat(opposingPaladin.getPowerModifier()).isZero();
    }

    @Test
    void boostsCreaturesSharingANameWithACreatureCardInTheGraveyard() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin()));

        advanceToCombat(player1);

        assertThat(liara.getPowerModifier()).isZero();
        assertThat(paladin.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void combatBoostWearsOffAtEndOfTurn() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        harness.setGraveyard(player1, List.of(new LiaraOfTheFlamingFist()));

        advanceToCombat(player1);
        assertThat(liara.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(liara.getPowerModifier()).isZero();
    }

    @Test
    void activatedAbilityGrantsFirstStrikeAndDoubleTeamOnlyOnce() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent target = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isTrue();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void activatedAbilityCannotTargetTheSourceOrAnOpponentCreature() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent opposingPaladin = addCreatureReady(player2, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, liara.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingPaladin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken creature you control");
    }

    @Test
    void boostsOwnCreaturesDuringOpponentsCombat() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.setGraveyard(player1, List.of(new SteadfastPaladin()));

        advanceToCombat(player2);

        assertThat(paladin.getPowerModifier()).isEqualTo(1);
        assertThat(paladin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void opponentsCreaturesAndGraveyardDoNotQualifyOwnCreatures() {
        Permanent liara = addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        addCreatureReady(player2, new SteadfastPaladin());
        harness.setGraveyard(player2, List.of(new LiaraOfTheFlamingFist()));

        advanceToCombat(player1);

        assertThat(liara.getPowerModifier()).isZero();
        assertThat(paladin.getPowerModifier()).isZero();
    }

    @Test
    void checksMatchingNamesWhenCombatTriggerResolves() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent paladin = addCreatureReady(player1, new SteadfastPaladin());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new SteadfastPaladin()));

        resolveAllTriggers();

        assertThat(paladin.getPowerModifier()).isEqualTo(1);
    }

    @Test
    void activatedAbilityCannotTargetAToken() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        SteadfastPaladin token = new SteadfastPaladin();
        token.setToken(true);
        Permanent target = addCreatureReady(player1, token);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken creature you control");
    }

    @Test
    void activatedAbilityCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent target = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void grantedDoubleTeamConjuresACardAndThenRemovesItself() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent target = addCreatureReady(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof SteadfastPaladin);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void grantedKeywordsExpireAtEndOfTurn() {
        addCreatureReady(player1, new LiaraOfTheFlamingFist());
        Permanent target = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }

}
