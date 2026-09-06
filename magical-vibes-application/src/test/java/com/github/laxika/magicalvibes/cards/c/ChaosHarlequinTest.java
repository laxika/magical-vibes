package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgentOfStromgald;
import com.github.laxika.magicalvibes.cards.b.BalduvianTradingPost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Chaos Harlequin")
@CardUsed({ChaosHarlequin.class, AgentOfStromgald.class, BalduvianTradingPost.class})
class ChaosHarlequinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -4/-0 when the exiled card is a land")
    void shrinksOnLand() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new BalduvianTradingPost());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+0 when the exiled card is not a land")
    void pumpsOnNonland() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new AgentOfStromgald());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiles the top card of the library")
    void exilesTopCard() {
        addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new AgentOfStromgald());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new AgentOfStromgald());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does nothing when the library is empty at resolution")
    void doesNothingWhenLibraryIsEmptyAtResolution() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiles the top card when the ability resolves")
    void exilesTopCardOnResolution() {
        addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new AgentOfStromgald());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }
    @Test
    @DisplayName("Each activation uses the card exiled by its own resolution")
    void eachActivationUsesItsOwnExiledCard() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new AgentOfStromgald());
        gd.playerDecks.get(player1.getId()).addFirst(new BalduvianTradingPost());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, harlequin)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }
    private Permanent addHarlequin() {
        return addCreatureReady(player1, new ChaosHarlequin());
    }
}
