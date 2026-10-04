package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalconerAdept.class, TitanicGrowth.class, Unsubstantiate.class, InvasionOfZendikar.class})
class FalconerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Falconer Adept creates a tapped and attacking Bird token")
    void attackCreatesBirdToken() {
        addCreatureReady(player1, new FalconerAdept());

        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        List<Permanent> birdTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird"))
                .toList();
        assertThat(birdTokens).hasSize(1);

        Permanent bird = birdTokens.getFirst();
        assertThat(bird.getCard().getPower()).isEqualTo(1);
        assertThat(bird.getCard().getToughness()).isEqualTo(1);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(bird.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bird.isTapped()).isTrue();
        assertThat(bird.isAttacking()).isTrue();
        assertThat(bird.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(bird.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Falconer Adept does not create a Bird token without attacking")
    void noTokenWhenNotAttacking() {
        harness.addToBattlefield(player1, new FalconerAdept());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird")))
                .isEmpty();
    }

    @Test
    @DisplayName("Each attacking Falconer Adept creates its own Bird")
    void twoAttackersCreateTwoBirds() {
        addCreatureReady(player1, new FalconerAdept());
        addCreatureReady(player1, new FalconerAdept());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Bird")).hasSize(2).allSatisfy(bird -> {
            assertThat(bird.isTapped()).isTrue();
            assertThat(bird.isAttacking()).isTrue();
            assertThat(bird.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(bird.isAttackedThisTurn()).isFalse();
        });
    }

    @Test
    @DisplayName("The attack trigger creates a Bird even after Falconer Adept leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent adept = addCreatureReady(player1, new FalconerAdept());
        harness.setHand(player2, List.of(new Unsubstantiate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player2, 0, adept.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player1, "Falconer Adept");
            harness.assertInHand(player1, "Falconer Adept");
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Bird")).hasSize(1).allSatisfy(bird -> {
            assertThat(bird.isTapped()).isTrue();
            assertThat(bird.isAttacking()).isTrue();
            assertThat(bird.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("The Bird can attack a battle protected by the defending player")
    void birdCanAttackBattle() {
        addCreatureReady(player1, new FalconerAdept());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handlePermanentChosen(player1, battle.getId());
        });

        assertThat(findPermanents(player1, "Bird")).hasSize(1).allSatisfy(bird -> {
            assertThat(bird.isTapped()).isTrue();
            assertThat(bird.isAttacking()).isTrue();
            assertThat(bird.getAttackTarget()).isEqualTo(battle.getId());
        });
    }
}
