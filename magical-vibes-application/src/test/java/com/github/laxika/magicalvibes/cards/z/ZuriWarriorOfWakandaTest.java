package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZuriWarriorOfWakanda.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, Integer.parseInt(spell.getManaCost().replace("{", "").replace("}", "")));
        if (spell.getType() == CardType.ARTIFACT) {
            harness.castArtifact(player1, 0);
        } else {
            harness.castSorcery(player1, 0);
        }
        resolveAllTriggers();
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
