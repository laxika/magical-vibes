package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDalekEmperor.class, GrizzlyBears.class})
class TheDalekEmperorTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Daleks reduces the generic mana cost")
    void affinityForDaleksReducesGenericCost() {
        harness.addToBattlefield(player1, new TheDalekEmperor());
        harness.setHand(player1, List.of(new TheDalekEmperor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Dalek Emperor");
    }

    @Test
    @DisplayName("Other Daleks you control have haste")
    void otherDaleksHaveHaste() {
        Permanent emperor = harness.addToBattlefieldAndReturn(player1, new TheDalekEmperor());

        advanceToCombatAndResolve(player1);

        Permanent dalek = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(dalek.getCard().getSubtypes()).contains(CardSubtype.DALEK);
        assertThat(gqs.hasKeyword(gd, dalek, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, emperor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent can sacrifice a creature instead of giving you a Dalek")
    void opponentCanSacrificeCreature() {
        harness.addToBattlefield(player1, new TheDalekEmperor());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
