package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UtvaraHellkiteTest extends BaseCardTest {

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card, java.util.UUID owner) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(owner).add(permanent);
        return permanent;
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private List<Permanent> dragonTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Attacking with Utvara Hellkite itself creates a 6/6 flying Dragon token")
    void hellkiteTriggersOnItself() {
        addAttacker(new UtvaraHellkite(), player1.getId());

        beginCombat();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        List<Permanent> tokens = dragonTokens();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(6);
        assertThat(token.getEffectiveToughness()).isEqualTo(6);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each attacking Dragon triggers separately")
    void triggersOncePerAttackingDragon() {
        addAttacker(new UtvaraHellkite(), player1.getId());
        addAttacker(new ShivanDragon(), player1.getId());

        beginCombat();
        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dragonTokens()).hasSize(2);
    }

    @Test
    @DisplayName("A non-Dragon attacker does not trigger the ability")
    void nonDragonAttackerDoesNotTrigger() {
        addAttacker(new UtvaraHellkite(), player1.getId());
        addAttacker(new GrizzlyBears(), player1.getId());

        beginCombat();
        // Only the Grizzly Bears attacks.
        gs.declareAttackers(gd, player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(dragonTokens()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's attacking Dragon does not trigger the ability")
    void opponentDragonDoesNotTrigger() {
        addAttacker(new UtvaraHellkite(), player1.getId());
        addAttacker(new ShivanDragon(), player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        assertThat(dragonTokens()).isEmpty();
    }
}
