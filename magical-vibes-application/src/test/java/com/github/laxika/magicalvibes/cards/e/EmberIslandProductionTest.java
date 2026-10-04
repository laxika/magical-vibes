package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberIslandProduction.class, GrizzlyBears.class, MirriCatWarrior.class})
class EmberIslandProductionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a nonlegendary 4/4 Hero copy of a creature you control")
    void createsHeroCopyOfOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        cast(0, target);

        Permanent token = tokenCopy(player1);
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CAT, CardSubtype.HERO);
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Creates a nonlegendary 2/2 Coward copy of an opponent's creature")
    void createsCowardCopyOfOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());
        cast(1, target);

        Permanent token = tokenCopy(player1);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CAT, CardSubtype.COWARD);
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Each mode enforces its controller restriction")
    void modesEnforceControllerRestrictions() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(opposingTarget.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copying does not copy counters or tapped status")
    void doesNotCopyPermanentState() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();

        cast(0, target);

        Permanent token = tokenCopy(player1);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("A target that leaves the battlefield produces no token")
    void removedTargetProducesNoToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent mode fails if its target comes under your control before resolution")
    void rechecksTargetControllerOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copying a Coward token as a Hero retains the earlier added subtype")
    void copiesEarlierCopyExceptions() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());
        cast(1, target);
        Permanent coward = tokenCopy(player1);

        cast(0, coward);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        Permanent hero = tokens.stream().filter(permanent -> permanent != coward).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(hero.getCard().getSubtypes()).contains(CardSubtype.CAT, CardSubtype.WARRIOR,
                CardSubtype.COWARD, CardSubtype.HERO);
        assertThat(hero.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new EmberIslandProduction()));
        addMana();
        harness.castModalSorcery(player1, 0, mode, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent tokenCopy(com.github.laxika.magicalvibes.model.Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
