package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZuriWarriorOfWakanda.class, GrizzlyBears.class, SolemnSimulacrum.class, ThranDynamo.class})
class ZuriWarriorOfWakandaTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control for an artifact spell with mana value 4 or greater")
    void highManaArtifactSpellPutsCountersOnControlledCreatures() {
        Permanent zuri = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAndResolve(artifactSpell("Large Artifact", 4));

        assertThat(zuri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for an artifact spell with mana value less than 4 or a nonartifact spell")
    void doesNotTriggerForNonmatchingSpells() {
        Permanent zuri = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castAndResolve(artifactSpell("Small Artifact", 3));
        castAndResolve(spell("Large Sorcery", CardType.SORCERY, 4));

        assertThat(zuri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castAndResolve(Card spell) {
        harness.castFromHand(player1, spell, spell.getManaCost());
        resolveAllTriggers();
    }

    @Test
    void artifactCreatureTriggersBeforeItEntersTheBattlefield() {
        Permanent zuri = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent existingCreature = addCreatureReady(player1, new SolemnSimulacrum());
        SolemnSimulacrum spell = new SolemnSimulacrum();

        harness.castFromHand(player1, spell, "{4}");

        assertThat(gd.stack).hasSize(2);
        assertThat(zuri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(zuri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(spell.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(spell.getId()))
                .singleElement().satisfies(p ->
                        assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void opponentsArtifactSpellDoesNotTrigger() {
        Permanent zuri = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new ThranDynamo(), "{4}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(zuri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachQualifyingCastAddsCountersOnlyToCreatures() {
        Permanent zuri = addCreatureReady(player1, new ZuriWarriorOfWakanda());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranDynamo());

        harness.castFromHand(player1, new ThranDynamo(), "{4}");
        resolveAllTriggers();
        harness.castFromHand(player1, new ThranDynamo(), "{4}");
        resolveAllTriggers();

        assertThat(zuri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private static Card artifactSpell(String name, int manaValue) {
        return spell(name, CardType.ARTIFACT, manaValue);
    }

    private static Card spell(String name, CardType type, int manaValue) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{" + manaValue + "}");
        return card;
    }
}
