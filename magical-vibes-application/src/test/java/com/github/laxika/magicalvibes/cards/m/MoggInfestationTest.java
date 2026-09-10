package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Burgeoning;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({MoggInfestation.class, MoggFlunkies.class, Burgeoning.class})
class MoggInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target player's creatures and gives that player two Goblins per creature")
    void destroysTargetCreaturesAndCreatesGoblinTokensForTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.addToBattlefield(player2, new Burgeoning());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mogg Flunkies")).isEqualTo(1);
        assertThat(countPermanents(player1, "Goblin")).isZero();
        assertThat(countPermanents(player2, "Mogg Flunkies")).isZero();
        List<Permanent> goblins = findPermanents(player2, "Goblin");
        assertThat(goblins).hasSize(4);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        });
        harness.assertOnBattlefield(player2, "Burgeoning");
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetCaster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MoggFlunkies());
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mogg Flunkies")).isZero();
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(countPermanents(player2, "Mogg Flunkies")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no Goblins when the target player controls no creatures")
    void createsNoTokensWhenTargetHasNoCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new Burgeoning());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Goblin")).isZero();
        harness.assertOnBattlefield(player2, "Burgeoning");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new MoggFlunkies());
        harness.setHand(player1, List.of(new MoggInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        var permanentId = findPermanent(player2, "Mogg Flunkies").getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
