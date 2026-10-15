package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NaturalObsolescence;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RenegadeMap;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MechanizedProduction.class, NaturalObsolescence.class, Ornithopter.class, RenegadeMap.class})
class MechanizedProductionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of the enchanted artifact at your upkeep")
    void createsTokenCopyOfEnchantedArtifact() {
        Permanent artifact = addArtifact(new Ornithopter());
        castProduction(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Wins after the token makes eight same-name artifacts")
    void winsWithEightArtifactsOfTheSameName() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castProduction(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not win before the token creates eight same-name artifacts")
    void doesNotWinBelowEightArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castProduction(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Cannot enchant an artifact controlled by an opponent")
    void cannotEnchantOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new MechanizedProduction()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    @Test
    @DisplayName("Wins with eight artifacts sharing a name different from the enchanted artifact")
    void winsWithArtifactsOfAnotherName() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        castProduction(artifact);
        assertThat(gd.winnerPlayerId).isNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not count opponents' artifacts toward eight of the same name")
    void doesNotCountOpponentsArtifacts() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.addToBattlefield(player2, new Ornithopter());
        castProduction(gd.playerBattlefields.get(player1.getId()).getFirst());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castProduction(artifact);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Creates no token when the enchanted artifact leaves before the Aura")
    void createsNoTokenWhenEnchantedArtifactLeavesFirst() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castProduction(artifact);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new NaturalObsolescence()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Mechanized Production");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Still checks for a win when the enchanted artifact leaves before resolution")
    void stillWinsWhenNoTokenCanBeCreated() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        castProduction(artifact);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new NaturalObsolescence()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Mechanized Production");
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addArtifact(Card artifact) {
        return harness.addToBattlefieldAndReturn(player1, artifact);
    }

    private void castProduction(Permanent artifact) {
        harness.setHand(player1, List.of(new MechanizedProduction()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
    }
}
