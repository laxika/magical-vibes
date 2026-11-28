package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CreateTokenCopyOfCardEffectHandler} over the cards a step exiled. No card exiles a card face down and then
 * copies it, so the exile step is recorded by hand.
 */
@CardUsed({GrizzlyBears.class})
class CreateTokenCopyOfCardEffectHandlerTest extends BaseCardTest {

    @Test
    @DisplayName("A token copy of a card exiled face down copies nothing of its hidden face")
    void copyOfFaceDownExiledCardHasNoCharacteristics() {
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, null, true);

        resolveCopyOfExiledCards(bears);

        Permanent token = onlyToken();
        assertThat(token.getCard().getName()).isNotEqualTo("Grizzly Bears");
        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("A token copy of a card exiled face up copies the card")
    void copyOfFaceUpExiledCardCopiesTheCard() {
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears);

        resolveCopyOfExiledCards(bears);

        Permanent token = onlyToken();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    private void resolveCopyOfExiledCards(Card exiled) {
        CardEffect copy = CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                new CreateTokenCopyOfTargetPermanentEffect());
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Test ability", List.of(copy));
        entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(exiled.getId()), List.of(player2.getId())));
        gd.stack.add(entry);
        CreateTokenCopyOfCardEffectHandler handler =
                GameTestEngineContext.get().getBean(CreateTokenCopyOfCardEffectHandler.class);
        harness.inMutationScope(() -> handler.resolve(gd, entry, copy));
    }

    private Permanent onlyToken() {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        return tokens.getFirst();
    }
}
