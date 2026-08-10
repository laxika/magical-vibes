package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThopterSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ThopterSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent squadron = findPermanent(player1, "Thopter Squadron");
        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(squadron.getEffectivePower()).isEqualTo(3);
        assertThat(squadron.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates a 1/1 colorless Thopter artifact creature token")
    void removingCounterCreatesThopterToken() {
        Permanent squadron = addReadySquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, squadron), 0, null, null);
        harness.passBothPriorities();

        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent token = findThopterToken();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another Thopter puts a +1/+1 counter on Thopter Squadron")
    void sacrificingAnotherThopterAddsCounter() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ThopterFoundry());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, foundry), 0, null, null);
        harness.handlePermanentChosen(player1, spellbook.getId());
        harness.passBothPriorities();

        Permanent squadron = addReadySquadron(player1);
        Permanent token = findThopterToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null);
        harness.passBothPriorities();

        assertThat(squadron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.assertOnBattlefield(player1, "Thopter Squadron");
    }

    @Test
    @DisplayName("The sacrifice ability cannot sacrifice Thopter Squadron itself")
    void sacrificeAbilityRequiresAnotherThopter() {
        Permanent squadron = addReadySquadron(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, squadron), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySquadron(Player player) {
        Permanent squadron = new Permanent(new ThopterSquadron());
        squadron.setSummoningSick(false);
        squadron.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        gd.playerBattlefields.get(player.getId()).add(squadron);
        return squadron;
    }

    private Permanent findThopterToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.THOPTER))
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
