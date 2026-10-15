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

import java.util.List;

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
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+0 when the library is empty at resolution")
    void pumpsWhenLibraryIsEmptyAtResolution() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(4);
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

    @Test
    @DisplayName("An activation after the last land is exiled does not reuse that land")
    void emptyLibraryDoesNotReusePreviouslyExiledLand() {
        Permanent harlequin = addHarlequin();
        BalduvianTradingPost land = new BalduvianTradingPost();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(-2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("The land penalty wears off at end of turn")
    void landPenaltyWearsOff() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new BalduvianTradingPost());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiles the card even if its source has left the battlefield")
    void exilesAfterSourceLeavesWithoutBoostingAnotherHarlequin() {
        Permanent original = addHarlequin();
        AgentOfStromgald topCard = new AgentOfStromgald();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());
        Permanent replacement = addHarlequin();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(4);
    }

    private Permanent addHarlequin() {
        return addCreatureReady(player1, new ChaosHarlequin());
    }
}
