package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SourbreadAuntie.class)
class SourbreadAuntieTest extends BaseCardTest {

    @Test
    void acceptingEnterTriggerBlightsChosenCreatureAndCreatesGoblinTokens() {
        castSourbreadAuntie();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent auntiePermanent = findPermanent(player1, "Sourbread Auntie");
        assertThat(auntiePermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(goblin.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
            assertThat(goblin.getEffectivePower()).isEqualTo(1);
            assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void decliningEnterTriggerDoesNotBlightOrCreateTokens() {
        SourbreadAuntie auntie = castSourbreadAuntie();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == auntie
                        && permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) == 0);
    }

    @Test
    void canBlightAnotherCreatureEvenWhenTheCountersKillIt() {
        SourbreadAuntie otherAuntie = new SourbreadAuntie();
        Permanent other = harness.addToBattlefieldAndReturn(player1, otherAuntie);
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SourbreadAuntie());
        SourbreadAuntie auntie = castSourbreadAuntie();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(other.getId()).doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, other.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherAuntie);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == auntie
                        && permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) == 0);
        assertThat(opponent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    private SourbreadAuntie castSourbreadAuntie() {
        SourbreadAuntie auntie = new SourbreadAuntie();
        harness.setHand(player1, List.of(auntie));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        return auntie;
    }
}
