package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DeathlessBehemoth.class)
class DeathlessBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to hand by sacrificing two Eldrazi Scions")
    void returnsFromGraveyardBySacrificingTwoEldraziScions() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        Permanent firstScion = harness.addToBattlefieldAndReturn(player1, createScion());
        Permanent secondScion = harness.addToBattlefieldAndReturn(player1, createScion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathless Behemoth");
        harness.assertInGraveyard(player1, "Eldrazi Scion");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstScion, secondScion);
    }

    @Test
    @DisplayName("Does not accept Eldrazi Spawn for the sacrifice cost")
    void doesNotAcceptEldraziSpawnForTheSacrificeCost() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.addToBattlefield(player1, createScion());
        harness.addToBattlefield(player1, createSpawn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void canOnlyBeActivatedAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.addToBattlefield(player1, createScion());
        harness.addToBattlefield(player1, createScion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Card createScion() {
        Card card = new Card();
        card.setName("Eldrazi Scion");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ELDRAZI, CardSubtype.SCION));
        return card;
    }

    private Card createSpawn() {
        Card card = createScion();
        card.setName("Eldrazi Spawn");
        card.setSubtypes(List.of(CardSubtype.ELDRAZI, CardSubtype.SPAWN));
        return card;
    }
}
