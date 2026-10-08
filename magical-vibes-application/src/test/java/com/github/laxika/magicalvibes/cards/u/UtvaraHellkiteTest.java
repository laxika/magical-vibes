package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UtvaraHellkite.class, ShivanDragon.class, GrizzlyBears.class, UltimatePrice.class})
class UtvaraHellkiteTest extends BaseCardTest {

    private List<Permanent> dragonTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Attacking with Utvara Hellkite itself creates a 6/6 flying Dragon token")
    void hellkiteTriggersOnItself() {
        addCreatureReady(player1, new UtvaraHellkite());

        declareAttackers(List.of(0));
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
        addCreatureReady(player1, new UtvaraHellkite());
        addCreatureReady(player1, new ShivanDragon());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dragonTokens()).hasSize(2);
    }

    @Test
    @DisplayName("A non-Dragon attacker does not trigger the ability")
    void nonDragonAttackerDoesNotTrigger() {
        addCreatureReady(player1, new UtvaraHellkite());
        addCreatureReady(player1, new GrizzlyBears());

        // Only the Grizzly Bears attacks.
        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(dragonTokens()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's attacking Dragon does not trigger the ability")
    void opponentDragonDoesNotTrigger() {
        addCreatureReady(player1, new UtvaraHellkite());
        addCreatureReady(player2, new ShivanDragon());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(dragonTokens()).isEmpty();
    }

    @Test
    @DisplayName("Two Hellkites each trigger for every attacking Dragon")
    void multipleHellkitesTriggerIndependently() {
        addCreatureReady(player1, new UtvaraHellkite());
        addCreatureReady(player1, new UtvaraHellkite());

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        assertThat(dragonTokens()).hasSize(4);
    }

    @Test
    @DisplayName("A Hellkite that stays back still triggers for another attacking Dragon")
    void nonAttackingHellkiteStillTriggers() {
        addCreatureReady(player1, new UtvaraHellkite());
        addCreatureReady(player1, new UtvaraHellkite());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(dragonTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Created Dragons enter untapped and not attacking without retriggering Hellkite")
    void tokensEnterOutsideCombat() {
        addCreatureReady(player1, new UtvaraHellkite());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(dragonTokens()).hasSize(1);
        Permanent token = dragonTokens().getFirst();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger creates a Dragon even after Hellkite is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent hellkite = addCreatureReady(player1, new UtvaraHellkite());
        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, hellkite.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Utvara Hellkite");
        resolveAllTriggers();

        assertThat(dragonTokens()).hasSize(1);
    }
}
