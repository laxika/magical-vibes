package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BelenonWarAnthem;
import com.github.laxika.magicalvibes.cards.s.SwordswornCavalier;
import com.github.laxika.magicalvibes.cards.z.ZurEternalSchemer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelenonWarAnthem.class, SwordswornCavalier.class, InvasionOfBelenon.class,
        ZurEternalSchemer.class})
class InvasionOfBelenonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and creates a 2/2 white and blue Knight with vigilance")
    void entersCreatesKnightToken() {
        castInvasion();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes()).contains(com.github.laxika.magicalvibes.model.CardSubtype.KNIGHT);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardColor.WHITE,
                com.github.laxika.magicalvibes.model.CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Defeat exiles the Siege and casts Belenon War Anthem transformed")
    void defeatCastsBackFace() {
        castInvasion();

        Permanent battle = findPermanentByName(player1, "Invasion of Belenon");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent anthem = findPermanentByName(player1, "Belenon War Anthem");
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new SwordswornCavalier());
        assertThat(gqs.getEffectivePower(gd, cavalier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cavalier)).isEqualTo(2);
        assertThat(anthem.isTransformed()).isTrue();
    }

    @Test
    void anthemBoostsOnlyItsControllersCreaturesAndStopsWhenItLeaves() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new BelenonWarAnthem());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SwordswornCavalier());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SwordswornCavalier());

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(anthem);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(1);
    }

    @Test
    void transformedAnthemBoostsItselfWhenAnimated() {
        castInvasion();
        Permanent battle = findPermanentByName(player1, "Invasion of Belenon");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent anthem = findPermanentByName(player1, "Belenon War Anthem");
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zur),
                null, anthem.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, anthem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anthem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anthem)).isEqualTo(4);
    }

    @Test
    void canDeclineCastingTheDefeatedSiege() {
        castInvasion();
        Permanent battle = findPermanentByName(player1, "Invasion of Belenon");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Invasion of Belenon");
        harness.assertNotOnBattlefield(player1, "Belenon War Anthem");
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castInvasion() {
        harness.castFromHand(player1, new InvasionOfBelenon(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
