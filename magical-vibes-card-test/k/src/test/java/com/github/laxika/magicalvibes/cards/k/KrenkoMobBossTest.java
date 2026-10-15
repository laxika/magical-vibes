package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MoggFlunkies;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrenkoMobBoss.class, MoggFlunkies.class, ElvishVisionary.class, Unsummon.class})
class KrenkoMobBossTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Goblin token per Goblin controlled, counting himself")
    void createsTokensEqualToGoblinCount() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        addCreatureReady(player1, new MoggFlunkies());
        addCreatureReady(player1, new ElvishVisionary());

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();

        var tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(p -> {
            assertThat(p.getCard().getPower()).isEqualTo(1);
            assertThat(p.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Goblins an opponent controls are not counted")
    void ignoresOpponentGoblins() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        addCreatureReady(player2, new MoggFlunkies());
        addCreatureReady(player2, new MoggFlunkies());

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    @Test
    void countsGoblinsAddedBeforeResolution() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());

        harness.activateAbility(player1, indexOf(krenko), null, null);
        assertThat(krenko.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        addCreatureReady(player1, new MoggFlunkies());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }

    @Test
    void countsOnlyGoblinsRemainingAtResolution() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        Permanent goblin = addCreatureReady(player1, new MoggFlunkies());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.castAndResolveInstant(player2, 0, goblin.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Mogg Flunkies");
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    @Test
    void abilityResolvesAfterKrenkoLeavesBattlefield() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        addCreatureReady(player1, new MoggFlunkies());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.castAndResolveInstant(player2, 0, krenko.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Krenko, Mob Boss");
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    void createsNoTokensWhenNoGoblinsRemain() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.castAndResolveInstant(player2, 0, krenko.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Krenko, Mob Boss");
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void countsPreviouslyCreatedGoblinTokens() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);

        krenko.untap();
        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new KrenkoMobBoss());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(krenko), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
