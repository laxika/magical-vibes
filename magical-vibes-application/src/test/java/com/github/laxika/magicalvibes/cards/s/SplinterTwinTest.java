package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplinterTwin.class, SporecapSpider.class, NestInvader.class})
class SplinterTwinTest extends BaseCardTest {

    @Test
    void createsHastyTokenCopyAndExilesItAtNextEndStep() {
        Permanent creature = enchantedCreature(player1, player1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent token = tokenCopy(player1);
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(token.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        advanceToEndStep();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void creatureControllerGetsTokenEvenWhenOpponentControlsAura() {
        enchantedCreature(player1, player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tokenCopy(player1).hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void summoningSickCreatureCannotPayTapCost() {
        Permanent creature = enchantedCreature(player1, player1);
        creature.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void removingAuraInResponseDoesNotStopCopyOrDelayedExile() {
        enchantedCreature(player1, player1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(1);

        harness.passBothPriorities();
        Permanent token = tokenCopy(player1);
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void removingCreatureInResponseStillCreatesCopy() {
        Permanent creature = enchantedCreature(player1, player1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(tokenCopy(player1).hasKeyword(Keyword.REACH)).isTrue();
        assertThat(tokenCopy(player1).hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void tokenCreatedDuringEndStepWaitsUntilFollowingEndStep() {
        enchantedCreature(player1, player1);
        advanceToEndStep();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = tokenCopy(player1);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void delayedExileRetainsAbilityControllerAfterTokenChangesControl() {
        enchantedCreature(player1, player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = tokenCopy(player1);
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    void resolvingAuraGrantsCopyAbilityToItsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());
        creature.setSummoningSick(false);
        harness.setHand(player1, java.util.List.of(new SplinterTwin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tokenCopy(player1).hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void copiedEnterAbilityCreatesSpawnThatIsNotExiledWithCopy() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SplinterTwin());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(tokenCopy(player1).hasKeyword(Keyword.HASTE)).isFalse();
    }
    private Permanent enchantedCreature(Player creatureController, Player auraController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new SporecapSpider());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new SplinterTwin());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    private Permanent tokenCopy(Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
