package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.s.SternLesson;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzaPowerstoneProdigy.class, ArgothianSprite.class, EnergyRefractor.class, SternLesson.class})
class UrzaPowerstoneProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and discards a card when activated")
    void drawsThenDiscards() {
        addUrza();
        harness.setHand(player1, List.of(new ArgothianSprite()));
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Argothian Sprite");
        harness.assertInHand(player1, "Energy Refractor");
        assertThat(countPermanents(player1, "Powerstone")).isZero();
    }

    @Test
    @DisplayName("Discarding an artifact creates a tapped Powerstone token")
    void artifactDiscardCreatesTappedPowerstone() {
        addUrza();
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE))
                .toList();
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
        assertThat(powerstones.getFirst().getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("Creates at most one Powerstone from artifact discards each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent urza = addUrza();
        harness.setHand(player1, List.of(new EnergyRefractor(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        lootAndDiscard(0, urza);
        lootAndDiscard(0, urza);

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
    }

    @Test
    void nonartifactDiscardDoesNotConsumeTheTriggerForTheTurn() {
        Permanent urza = addUrza();
        harness.setHand(player1, List.of(new ArgothianSprite(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        lootAndDiscard(0, urza);
        assertThat(countPermanents(player1, "Powerstone")).isZero();
        lootAndDiscard(0, urza);

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
    }

    @Test
    void artifactDiscardFromAnotherSpellTriggersUrza() {
        addUrza();
        harness.setHand(player1, List.of(new SternLesson(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Energy Refractor");
    }

    @Test
    void opponentsArtifactDiscardDoesNotTriggerUrza() {
        addUrza();
        harness.setHand(player2, List.of(new SternLesson(), new EnergyRefractor()));
        harness.setLibrary(player2, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(countPermanents(player1, "Powerstone")).isZero();
        assertThat(countPermanents(player2, "Powerstone")).isEqualTo(1);
    }

    @Test
    void canTriggerAgainOnTheOpponentsTurn() {
        Permanent urza = addUrza();
        harness.setHand(player1, List.of(new EnergyRefractor(), new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.setLibrary(player2, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        lootAndDiscard(0, urza);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        lootAndDiscard(0, urza);

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(2);
    }

    @Test
    void canDiscardTheCardJustDrawnFromAnEmptyHand() {
        addUrza();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Energy Refractor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
    }

    @Test
    void powerstoneManaCannotCastNonartifactsButCanPayForUrzasAbility() {
        Permanent urza = addUrza();
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        lootAndDiscard(0, urza);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 1, null, null);
        lootAndDiscard(0, urza);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
    }

    private Permanent addUrza() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPowerstoneProdigy());
        urza.setSummoningSick(false);
        return urza;
    }

    private void lootAndDiscard(int discardIndex, Permanent urza) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, discardIndex);
        harness.passBothPriorities();
        urza.untap();
    }
}
