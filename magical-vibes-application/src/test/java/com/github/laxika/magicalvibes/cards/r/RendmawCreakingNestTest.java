package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendmawCreakingNest.class, DarksteelMyr.class, DryadArbor.class, GrizzlyBears.class})
class RendmawCreakingNestTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by giving each player a tapped, goaded Bird")
    void entersAndCreatesGoadedBirds() {
        castRendmaw();

        assertThat(birds(player1)).hasSize(1);
        assertThat(birds(player2)).hasSize(1);
        assertThat(birds(player1)).allSatisfy(bird -> {
            assertThat(bird.isTapped()).isTrue();
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        });

        Permanent bird = birds(player1).getFirst();
        bird.untap();
        bird.setSummoningSick(false);
        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Triggers when a two-type spell or land is played")
    void triggersForTwoTypeSpellAndLand() {
        addRendmawToBattlefield();

        harness.setHand(player1, List.of(new DarksteelMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(birds(player1)).hasSize(1);
        assertThat(birds(player2)).hasSize(1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DryadArbor()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(birds(player1)).hasSize(2);
        assertThat(birds(player2)).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for a single-type spell")
    void doesNotTriggerForSingleTypeSpell() {
        addRendmawToBattlefield();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(birds(player1)).isEmpty();
        assertThat(birds(player2)).isEmpty();
    }

    private void castRendmaw() {
        harness.setHand(player1, List.of(new RendmawCreakingNest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addRendmawToBattlefield() {
        harness.addToBattlefield(player1, new RendmawCreakingNest());
    }

    private List<Permanent> birds(Player player) {
        return findPermanents(player, "Bird").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
