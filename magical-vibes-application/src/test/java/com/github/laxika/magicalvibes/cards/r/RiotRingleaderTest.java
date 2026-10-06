package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiotRingleader.class})
class RiotRingleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new RiotRingleader());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Riot Ringleader"));
    }

    @Test
    @DisplayName("Boosts itself, being a Human")
    void boostsItself() {
        Permanent ringleader = addCreatureReady(player1, new RiotRingleader());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ringleader.getPowerModifier()).isEqualTo(1);
        assertThat(ringleader.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boosts other Humans you control, even ones that are not attacking")
    void boostsNonAttackingHumans() {
        addCreatureReady(player1, new RiotRingleader());
        Permanent human = addCreatureReady(player1, createCreature("Home Guard", CardSubtype.HUMAN));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not boost non-Human creatures you control")
    void doesNotBoostNonHumans() {
        addCreatureReady(player1, new RiotRingleader());
        Permanent goblin = addCreatureReady(player1, createCreature("Goblin Grunt", CardSubtype.GOBLIN));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(goblin.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Does not boost Humans an opponent controls")
    void doesNotBoostOpponentHumans() {
        addCreatureReady(player1, new RiotRingleader());
        Permanent enemyHuman = addCreatureReady(player2, createCreature("Enemy Soldier", CardSubtype.HUMAN));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(enemyHuman.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent ringleader = addCreatureReady(player1, new RiotRingleader());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ringleader.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ringleader.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Humans entering before the attack trigger resolves receive the boost")
    void boostsHumansPresentAtResolution() {
        addCreatureReady(player1, new RiotRingleader());
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        Permanent newcomer = addCreatureReady(player1, new RiotRingleader());
        resolveAllTriggers();

        assertThat(newcomer.getPowerModifier()).isEqualTo(1);
        assertThat(newcomer.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Humans entering after resolution do not receive the boost")
    void doesNotBoostHumansEnteringAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new RiotRingleader());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent newcomer = addCreatureReady(player1, new RiotRingleader());

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(newcomer.getPowerModifier()).isZero();
        assertThat(newcomer.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Two attacking Ringleaders each boost all your Humans")
    void multipleAttackTriggersStack() {
        Permanent first = addCreatureReady(player1, new RiotRingleader());
        Permanent second = addCreatureReady(player1, new RiotRingleader());
        Permanent nonAttacker = addCreatureReady(player1, new RiotRingleader());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        for (Permanent human : List.of(first, second, nonAttacker)) {
            assertThat(human.getPowerModifier()).isEqualTo(2);
            assertThat(human.getToughnessModifier()).isZero();
        }
    }

    private Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(subtype));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
