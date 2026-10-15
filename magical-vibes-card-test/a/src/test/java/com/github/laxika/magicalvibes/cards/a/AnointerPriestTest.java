package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnointerPriest.class, BladeSplicer.class, GrizzlyBears.class})
class AnointerPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Gain 1 life when a creature token you control enters")
    void gainsLifeOnCreatureTokenEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AnointerPriest());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // Blade Splicer (nontoken) grants no life; only the Golem token does.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("No life gained when a nontoken creature you control enters")
    void noLifeOnNontokenCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AnointerPriest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears spell -> enters

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No life gained for an opponent's creature token")
    void noLifeOnOpponentCreatureToken() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AnointerPriest());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new AnointerPriest()));
        harness.addMana(player1, ManaColor.WHITE, 4);
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Anointer Priest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Anointer Priest"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Anointer Priest") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.CLERIC);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalm can only be activated at sorcery speed")
    void embalmOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new AnointerPriest()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Anointer Priest");
    }

    @Test
    @DisplayName("An embalmed Priest gains life for its own entry")
    void embalmedPriestTriggersForItself() {
        setUpEmbalm();
        harness.setLife(player1, 20);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Anointer Priest");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Both the existing Priest and the embalmed Priest trigger for the token")
    void existingAndEmbalmedPriestBothTrigger() {
        setUpEmbalm();
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AnointerPriest());

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Embalm cannot be activated during combat")
    void embalmCannotBeActivatedDuringCombat() {
        setUpEmbalm();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Anointer Priest");
    }
}
