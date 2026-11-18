package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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
    @DisplayName("Attacking puts the attack trigger on the stack")
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

        assertThat(PermanentGrants.powerBoostOf(gd, ringleader)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ringleader)).isZero();
    }

    @Test
    @DisplayName("Boosts other Humans you control, even ones that are not attacking")
    void boostsNonAttackingHumans() {
        addCreatureReady(player1, new RiotRingleader());
        Permanent human = addCreatureReady(player1, createCreature("Home Guard", CardSubtype.HUMAN));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, human)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isZero();
    }

    @Test
    @DisplayName("Does not boost non-Human creatures you control")
    void doesNotBoostNonHumans() {
        addCreatureReady(player1, new RiotRingleader());
        Permanent goblin = addCreatureReady(player1, createCreature("Goblin Grunt", CardSubtype.GOBLIN));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, goblin)).isZero();
    }

    @Test
    @DisplayName("Does not boost Humans an opponent controls")
    void doesNotBoostOpponentHumans() {
        addCreatureReady(player1, new RiotRingleader());
        Permanent enemyHuman = addCreatureReady(player2, createCreature("Enemy Soldier", CardSubtype.HUMAN));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, enemyHuman)).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent ringleader = addCreatureReady(player1, new RiotRingleader());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, ringleader)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ringleader)).isZero();
    }

    @Test
    @DisplayName("Humans entering before the attack trigger resolves receive the boost")
    void boostsHumansPresentAtResolution() {
        addCreatureReady(player1, new RiotRingleader());
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        Permanent newcomer = addCreatureReady(player1, new RiotRingleader());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isZero();
    }

    @Test
    @DisplayName("Humans entering after resolution do not receive the boost")
    void doesNotBoostHumansEnteringAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new RiotRingleader());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent newcomer = addCreatureReady(player1, new RiotRingleader());

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isZero();
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
            assertThat(PermanentGrants.powerBoostOf(gd, human)).isEqualTo(2);
            assertThat(PermanentGrants.toughnessBoostOf(gd, human)).isZero();
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
