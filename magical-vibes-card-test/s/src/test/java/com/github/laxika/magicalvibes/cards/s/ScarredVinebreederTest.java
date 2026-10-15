package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishPromenade;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarredVinebreeder.class, GoldmeadowHarrier.class, ElvishPromenade.class, SkeletalChangeling.class})
class ScarredVinebreederTest extends BaseCardTest {

    private Permanent setup(List<Card> graveyard, int manaAvailable) {
        Permanent vinebreeder = harness.addToBattlefieldAndReturn(player1, new ScarredVinebreeder());
        vinebreeder.setSummoningSick(false);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, manaAvailable);
        return vinebreeder;
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Activating prompts to choose an Elf card to exile")
    void promptsForElfExile() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder()), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Only Elf cards are valid to exile as the cost")
    void onlyElfCardsAreValid() {
        // Graveyard: index 0 non-Elf (Goldmeadow Harrier), index 1 Elf (Scarred Vinebreeder)
        Permanent vinebreeder = setup(List.of(new GoldmeadowHarrier(), new ScarredVinebreeder()), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Exiles the chosen Elf, pumps +3/+3, and consumes {2}{B}")
    void exilesElfAndPumps() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder()), 4);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Scarred Vinebreeder");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scarred Vinebreeder"));
        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(3);
        // 4 - 3 ({2}{B}) = 1 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The +3/+3 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder()), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(vinebreeder.getPowerModifier()).isZero();
        assertThat(vinebreeder.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without an Elf card in graveyard")
    void cannotActivateWithoutElfInGraveyard() {
        Permanent vinebreeder = setup(List.of(new GoldmeadowHarrier()), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(vinebreeder), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder()), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(vinebreeder), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("The Elf is exiled and mana is paid before the boost resolves")
    void paysCostsBeforeResolution() {
        Card elf = new ScarredVinebreeder();
        Permanent vinebreeder = setup(List.of(elf), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elf);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(vinebreeder.getPowerModifier()).isZero();
        assertThat(vinebreeder.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple activations each exile an Elf and their boosts accumulate")
    void repeatedActivationsAccumulate() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder(), new ScarredVinebreeder()), 6);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(vinebreeder.getPowerModifier()).isEqualTo(6);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder()), 3);
        vinebreeder.setSummoningSick(true);
        vinebreeder.tap();

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(3);
        assertThat(vinebreeder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Elf in the opponent's graveyard cannot pay the cost")
    void cannotExileOpponentsElf() {
        Permanent vinebreeder = setup(List.of(), 3);
        Card opponentElf = new ScarredVinebreeder();
        harness.setGraveyard(player2, List.of(opponentElf));

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(vinebreeder), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf");

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentElf);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three mana without black mana cannot pay the activation cost")
    void requiresBlackMana() {
        Permanent vinebreeder = setup(List.of(new ScarredVinebreeder()), 0);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(vinebreeder), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Elf kindred sorcery can pay the cost even though it is not a creature")
    void canExileNoncreatureElfCard() {
        Card elf = new ElvishPromenade();
        Permanent vinebreeder = setup(List.of(elf), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elf);
        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("A changeling is an Elf in the graveyard and can pay the cost")
    void canExileChangelingCard() {
        Card elf = new SkeletalChangeling();
        Permanent vinebreeder = setup(List.of(elf), 3);

        harness.activateAbility(player1, idxOf(vinebreeder), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elf);
        assertThat(vinebreeder.getPowerModifier()).isEqualTo(3);
        assertThat(vinebreeder.getToughnessModifier()).isEqualTo(3);
    }
}
