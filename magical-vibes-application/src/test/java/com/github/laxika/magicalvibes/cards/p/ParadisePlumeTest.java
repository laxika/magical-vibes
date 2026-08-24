package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParadisePlume.class, GrizzlyBears.class, SuntailHawk.class})
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
    @DisplayName("A player casting a spell of the chosen color may gain 1 life")
    void gainsLifeWhenAnyPlayerCastsChosenColorSpell() {
        harness.addToBattlefield(player1, new ParadisePlume());
        Permanent plume = findPermanent(player1, "Paradise Plume");
        plume.setChosenColor(CardColor.GREEN);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Declining Paradise Plume's may ability gains no life")
    void mayDeclinesLifeGain() {
        harness.addToBattlefield(player1, new ParadisePlume());
        Permanent plume = findPermanent(player1, "Paradise Plume");
        plume.setChosenColor(CardColor.GREEN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Paradise Plume"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
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
        harness.addToBattlefield(player1, new ParadisePlume());
        Permanent plume = findPermanent(player1, "Paradise Plume");
        plume.setChosenColor(CardColor.GREEN);
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
