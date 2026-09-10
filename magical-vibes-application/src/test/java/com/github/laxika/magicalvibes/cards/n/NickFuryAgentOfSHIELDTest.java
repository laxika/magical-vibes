package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CaptainMarvelEarthsProtector;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NickFuryAgentOfSHIELD.class, CaptainMarvelEarthsProtector.class, Shock.class})
class NickFuryAgentOfSHIELDTest extends BaseCardTest {

    @Test
    void powerUpPutsTwoCountersAndAHeroOntoTheBattlefield() {
        Permanent nickFury = harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        Card hero = new CaptainMarvelEarthsProtector();
        setLibrary(hero, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(hero.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));

        assertThat(nickFury.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(hero.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void powerUpMayPutNothingOntoTheBattlefield() {
        Permanent nickFury = harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(nickFury.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        harness.enterBattlefieldAndReturn(player1, new NickFuryAgentOfSHIELD());
        addDiscountedPowerUpMana();
        addDiscountedPowerUpMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private void addDiscountedPowerUpMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
