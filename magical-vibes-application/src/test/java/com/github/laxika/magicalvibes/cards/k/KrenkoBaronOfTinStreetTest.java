package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrenkoBaronOfTinStreet.class, RagingGoblin.class, GrizzlyBears.class, Spellbook.class, Shatter.class})
class KrenkoBaronOfTinStreetTest extends BaseCardTest {

    @Test
    void sacrificesArtifactAndCountersOnlyOwnGoblins() {
        Permanent krenko = addCreatureReady(player1, new KrenkoBaronOfTinStreet());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(krenko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownGoblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentGoblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayPayTriggerCreatesHastyGoblin() {
        harness.addToBattlefield(player1, new KrenkoBaronOfTinStreet());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, java.util.List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player1, "Goblin");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
