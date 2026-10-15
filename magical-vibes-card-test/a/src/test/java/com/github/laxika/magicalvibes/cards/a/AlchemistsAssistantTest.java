package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlchemistsAssistant.class, DalkovanPackbeasts.class, Mountain.class})
class AlchemistsAssistantTest extends BaseCardTest {

    private void readyRenew() {
        harness.setGraveyard(player1, List.of(new AlchemistsAssistant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Renew puts a lifelink counter on target creature")
    void renewPutsLifelinkCounter() {
        Permanent bears = addCreatureReady(player1, new DalkovanPackbeasts());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Renew exiles Alchemist's Assistant as a cost")
    void renewExilesTheCard() {
        Permanent bears = addCreatureReady(player1, new DalkovanPackbeasts());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Alchemist's Assistant");
    }

    @Test
    @DisplayName("Renew can target an opponent's creature")
    void renewCanTargetOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new DalkovanPackbeasts());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Renew requires a creature target")
    void renewRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Renew can only be activated as a sorcery")
    void renewIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new DalkovanPackbeasts());
        readyRenew();


        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Renew pays exile immediately before the ability resolves")
    void renewPaysExileBeforeResolution() {
        Permanent target = addCreatureReady(player1, new DalkovanPackbeasts());
        readyRenew();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Alchemist's Assistant");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(source);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renew cannot be activated during upkeep")
    void renewCannotBeActivatedDuringUpkeep() {
        Permanent target = addCreatureReady(player1, new DalkovanPackbeasts());
        readyRenew();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Alchemist's Assistant");
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("Renew requires an empty stack even during your main phase")
    void renewRequiresEmptyStack() {
        Permanent target = addCreatureReady(player1, new DalkovanPackbeasts());
        readyRenew();
        harness.setGraveyard(player1, List.of(new AlchemistsAssistant(), new AlchemistsAssistant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renew cannot pay its black mana requirement with colorless mana")
    void renewRequiresBlackMana() {
        Permanent target = addCreatureReady(player1, new DalkovanPackbeasts());
        harness.setGraveyard(player1, List.of(new AlchemistsAssistant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Alchemist's Assistant");
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isZero();
    }
}
