package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FalconerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Falconer Adept creates a tapped and attacking Bird token")
    void attackCreatesBirdToken() {
        Permanent falconerAdept = new Permanent(new FalconerAdept());
        falconerAdept.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(falconerAdept);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
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
        assertThat(bird.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Falconer Adept does not create a Bird token without attacking")
    void noTokenWhenNotAttacking() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new FalconerAdept()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird")))
                .isEmpty();
    }
}
