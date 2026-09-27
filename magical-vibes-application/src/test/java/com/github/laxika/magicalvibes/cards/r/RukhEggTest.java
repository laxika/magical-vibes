package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RukhEgg.class, LightningBlast.class})
class RukhEggTest extends BaseCardTest {

    @Test
    @DisplayName("Death registers a delayed trigger; no token appears immediately")
    void deathRegistersDelayedTrigger() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Rukh Egg"));
        resolveAllTriggers(); // resolve the death trigger and register delayed token creation

        harness.assertInGraveyard(player1, "Rukh Egg");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateToken.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        // No token yet — it only appears at the next end step.
        harness.assertNotOnBattlefield(player1, "Bird");
    }

    @Test
    @DisplayName("Creates a 4/4 red Bird with flying at the beginning of the next end step")
    void createsBirdTokenAtNextEndStep() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Rukh Egg"));
        resolveAllTriggers(); // resolve the death trigger and register delayed token creation

        // Advance to the end step to fire the delayed trigger.
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities(); // resolve the token-creation trigger

        Permanent token = findPermanent(player1, "Bird");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }
}
