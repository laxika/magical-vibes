package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.ForgeDevil;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornetNest.class, GrizzlyBears.class, Shock.class, ForgeDevil.class, LightningStrike.class})
class HornetNestTest extends BaseCardTest {

    @Test
    @DisplayName("Hornet Nest cannot attack because it has defender")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new HornetNest());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shock dealing 2 damage to Hornet Nest creates two 1/1 flying deathtouch Insect tokens")
    void spellDamageCreatesThatManyTokens() {
        harness.addToBattlefield(player2, new HornetNest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nestId = harness.getPermanentId(player2, "Hornet Nest");
        harness.castAndResolveInstant(player1, 0, nestId);
        harness.passBothPriorities(); // Resolve the ON_DEALT_DAMAGE trigger

        harness.assertInGraveyard(player2, "Hornet Nest");

        List<Permanent> tokens = findPermanents(player2, "Insect");
        assertThat(tokens).hasSize(2);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getKeywords())
                .contains(Keyword.FLYING, Keyword.DEATHTOUCH);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Blocking a 2/2 attacker creates two Insect tokens")
    void combatDamageCreatesThatManyTokens() {
        harness.addToBattlefield(player2, new HornetNest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent nest = gd.playerBattlefields.get(player2.getId()).getFirst();
        nest.setSummoningSick(false);
        nest.setBlocking(true);
        nest.addBlockingTarget(0);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Insect")).hasSize(2);
        harness.assertInGraveyard(player2, "Hornet Nest");
    }

    @Test
    @DisplayName("Damage beyond lethal still creates tokens for the full damage dealt")
    void excessDamageCreatesTokensForFullAmount() {
        harness.addToBattlefield(player2, new HornetNest());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID nestId = harness.getPermanentId(player2, "Hornet Nest");
        harness.castAndResolveInstant(player1, 0, nestId);

        harness.assertInGraveyard(player2, "Hornet Nest");
        assertThat(findPermanents(player2, "Insect")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Insect")).hasSize(3);
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    @Test
    @DisplayName("Separate damage events each create tokens for that event's damage")
    void repeatedNonlethalDamageDoesNotCountPreviouslyMarkedDamage() {
        harness.addToBattlefield(player2, new HornetNest());
        harness.setHand(player1, List.of(new ForgeDevil(), new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID nestId = harness.getPermanentId(player2, "Hornet Nest");
        harness.castCreature(player1, 0, nestId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hornet Nest");
        assertThat(findPermanents(player2, "Insect")).hasSize(1);

        harness.castCreature(player1, 0, nestId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hornet Nest");
        assertThat(findPermanents(player2, "Insect")).hasSize(2);
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }
}
