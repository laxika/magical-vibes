package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirdPathIconoclast.class, Shock.class, GrizzlyBears.class, ChromaticStar.class,
        Divination.class, Ornithopter.class})
class ThirdPathIconoclastTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a colorless Soldier artifact creature token")
    void noncreatureSpellCreatesSoldierToken() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Soldier token")
    void creatureSpellCreatesNoSoldierToken() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    @DisplayName("Casting a noncreature artifact spell creates a Soldier token")
    void noncreatureArtifactSpellCreatesSoldierToken() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery creates a Soldier token")
    void sorceryCreatesSoldierToken() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    void tokenIsCreatedBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Soldier")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    void opponentSpellDoesNotTriggerIconoclast() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    void eachIconoclastTriggersForEveryNoncreatureSpell() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new ChromaticStar(), new ChromaticStar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(4);
    }

    @Test
    void artifactCreatureDoesNotTriggerIconoclast() {
        harness.addToBattlefield(player1, new ThirdPathIconoclast());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }
}
