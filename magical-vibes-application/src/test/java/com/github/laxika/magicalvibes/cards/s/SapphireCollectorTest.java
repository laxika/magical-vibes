package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SapphireCollector.class, MindStone.class, GrizzlyBears.class, Shock.class})
class SapphireCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Mox Sapphire on the second noncreature spell")
    void conjuresOnSecondNoncreatureSpell() {
        addSapphireCollector();
        harness.setHand(player1, List.of(new MindStone(), new GrizzlyBears(), new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Mox Sapphire");
    }

    @Test
    @DisplayName("The second noncreature spell trigger works only once")
    void secondNoncreatureSpellTriggerWorksOnlyOnce() {
        addSapphireCollector();
        harness.setHand(player1, List.of(
                new MindStone(), new MindStone(), new MindStone(), new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castAndResolveMindStone();
        castAndResolveMindStone();
        assertThat(countCardsInHand("Mox Sapphire")).isEqualTo(1);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        castAndResolveMindStone();
        castAndResolveMindStone();

        assertThat(countCardsInHand("Mox Sapphire")).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability grants flashback to a graveyard instant")
    void activatedAbilityGrantsFlashback() {
        addSapphireCollector();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("The activated ability cannot target a creature card")
    void activatedAbilityCannotTargetCreatureCard() {
        addSapphireCollector();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSapphireCollector() {
        addCreatureReady(player1, new SapphireCollector());
    }

    private void castAndResolveMindStone() {
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
    }

    private long countCardsInHand(String name) {
        return gd.playerHands.get(player1.getId()).stream()
                .map(Card::getName)
                .filter(name::equals)
                .count();
    }
}
