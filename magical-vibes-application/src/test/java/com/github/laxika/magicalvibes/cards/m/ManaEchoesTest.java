package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenSoulgazer;
import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.cards.i.Imagecrafter;
import com.github.laxika.magicalvibes.cards.l.LonelySandbar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaEchoes.class, FesteringGoblin.class, AvenSoulgazer.class, Imagecrafter.class, LonelySandbar.class})
class ManaEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Counts creatures sharing a type at resolution and adds one mana per creature")
    void countsMatchingCreaturesAtResolution() {
        harness.addToBattlefield(player1, new ManaEchoes());
        harness.addToBattlefield(player1, new FesteringGoblin());

        harness.enterBattlefieldAndReturn(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new FesteringGoblin());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the trigger produces no mana")
    void decliningTriggerProducesNoMana() {
        harness.addToBattlefield(player1, new ManaEchoes());

        harness.enterBattlefieldAndReturn(player1, new FesteringGoblin());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Counts only matching creatures controlled by Mana Echoes' controller")
    void countsOnlyMatchingCreaturesControlledByController() {
        harness.addToBattlefield(player1, new ManaEchoes());
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new AvenSoulgazer());

        harness.enterBattlefieldAndReturn(player2, new FesteringGoblin());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the entering creature's last known types if it leaves before resolution")
    void usesLastKnownTypesWhenEnteringCreatureLeaves() {
        harness.addToBattlefield(player1, new ManaEchoes());
        harness.addToBattlefield(player1, new FesteringGoblin());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new FesteringGoblin());
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getId().equals(entering.getId()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the entering creature's types as they exist when the ability resolves")
    void usesCurrentEnteringCreatureTypesAtResolution() {
        harness.addToBattlefield(player1, new ManaEchoes());
        addCreatureReady(player1, new Imagecrafter());
        harness.addToBattlefield(player1, new AvenSoulgazer());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new FesteringGoblin());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, null, entering.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, entering))
                .containsExactly(CardSubtype.CLERIC);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger Mana Echoes")
    void noncreaturePermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new ManaEchoes());
        harness.setHand(player1, List.of(new LonelySandbar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
