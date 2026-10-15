package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParadisePlume.class, AshcoatBear.class, BenalishCavalry.class})
class ParadisePlumeTest extends BaseCardTest {

    @Test
    @DisplayName("Paradise Plume asks for a color as it enters")
    void choosesColorOnEntry() {
        harness.setHand(player1, List.of(new ParadisePlume()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Paradise Plume").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("An opponent's matching spell lets Paradise Plume's controller gain life at resolution")
    void gainsLifeWhenAnyPlayerCastsChosenColorSpell() {
        Permanent plume = harness.addToBattlefieldAndReturn(player1, new ParadisePlume());
        plume.setChosenColor(CardColor.GREEN);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AshcoatBear()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Declining Paradise Plume's may ability gains no life")
    void mayDeclinesLifeGain() {
        Permanent plume = harness.addToBattlefieldAndReturn(player1, new ParadisePlume());
        plume.setChosenColor(CardColor.GREEN);
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Paradise Plume"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Paradise Plume taps for mana of its chosen color")
    void tapsForChosenColor() {
        Permanent plume = harness.addToBattlefieldAndReturn(player1, new ParadisePlume());
        plume.setSummoningSick(false);
        plume.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell without the chosen color does not trigger Paradise Plume")
    void doesNotTriggerForOtherColor() {
        Permanent plume = harness.addToBattlefieldAndReturn(player1, new ParadisePlume());
        plume.setChosenColor(CardColor.GREEN);
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each chosen color produces mana immediately, even on the turn the artifact enters")
    void producesChosenManaImmediatelyAfterEntry(CardColor color) {
        harness.setHand(player1, List.of(new ParadisePlume()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color.name())))
                .isEqualTo(1);
        assertThat(findPermanent(player1, "Paradise Plume").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a colorless artifact does not trigger a chosen-color ability")
    void colorlessSpellDoesNotTrigger() {
        Permanent plume = harness.addToBattlefieldAndReturn(player1, new ParadisePlume());
        plume.setChosenColor(CardColor.GREEN);
        harness.setHand(player1, List.of(new ParadisePlume()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
    }
}
