package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RukhEggTest extends BaseCardTest {

    @Test
    @DisplayName("Death registers a delayed trigger; no token appears immediately")
    void deathRegistersDelayedTrigger() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Rukh Egg"));
        harness.passBothPriorities(); // resolve Lightning Bolt — egg dies
        harness.passBothPriorities(); // resolve death trigger — register delayed token creation

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rukh Egg"));
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateToken.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        // No token yet — it only appears at the next end step.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bird"));
    }

    @Test
    @DisplayName("Creates a 4/4 red Bird with flying at the beginning of the next end step")
    void createsBirdTokenAtNextEndStep() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Rukh Egg"));
        harness.passBothPriorities(); // resolve Lightning Bolt — egg dies
        harness.passBothPriorities(); // resolve death trigger — register delayed token creation

        // Advance to the end step to fire the delayed trigger.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities(); // resolve the token-creation trigger

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bird"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }
}
