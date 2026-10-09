package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseHauler.class, RumblingBaloth.class, CanyonMinotaur.class, Duress.class})
class CorpseHaulerTest extends BaseCardTest {

    @Test
    void sacrificeIsPaidOnActivation() {
        RumblingBaloth target = new RumblingBaloth();
        prepareAbility(List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Corpse Hauler");
        harness.assertInGraveyard(player1, "Corpse Hauler");
        harness.assertInGraveyard(player1, "Rumbling Baloth");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetCardIds()).containsExactly(target.getId());
    }

    @Test
    void returnsCreatureToHand() {
        RumblingBaloth target = new RumblingBaloth();
        prepareAbility(List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Rumbling Baloth");
        harness.assertNotInGraveyard(player1, "Rumbling Baloth");
        harness.assertInGraveyard(player1, "Corpse Hauler");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnsOnlyTheCreatureTargetedOnActivation() {
        RumblingBaloth other = new RumblingBaloth();
        CanyonMinotaur target = new CanyonMinotaur();
        prepareAbility(List.of(other, target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Rumbling Baloth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetItselfToPayItsOwnSacrificeCost() {
        prepareAbility(List.of(new RumblingBaloth()));
        var sourceId = harness.getPermanentId(player1, "Corpse Hauler");

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(sourceId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Corpse Hauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutACreatureCardAlreadyInGraveyard() {
        prepareAbility(List.of());

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Corpse Hauler");
        harness.assertNotInGraveyard(player1, "Corpse Hauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotOmitTargetWhenCreatureIsAvailable() {
        prepareAbility(List.of(new RumblingBaloth()));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Corpse Hauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        RumblingBaloth target = new RumblingBaloth();
        harness.addToBattlefield(player1, new CorpseHauler());
        harness.setGraveyard(player1, List.of(target));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertOnBattlefield(player1, "Corpse Hauler");
    }

    @Test
    void cannotTargetNoncreatureCard() {
        Duress target = new Duress();
        prepareAbility(List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Corpse Hauler");
    }

    @Test
    void cannotTargetOpponentsCreatureCard() {
        RumblingBaloth target = new RumblingBaloth();
        prepareAbility(List.of(new CanyonMinotaur()));
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Corpse Hauler");
    }

    @Test
    void doesNotChooseAnotherCreatureWhenTargetLeavesGraveyard() {
        RumblingBaloth target = new RumblingBaloth();
        CanyonMinotaur other = new CanyonMinotaur();
        prepareAbility(List.of(target, other));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canyon Minotaur");
        harness.assertNotInHand(player1, "Canyon Minotaur");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canActivateOnOpponentsTurnWhileSummoningSick() {
        RumblingBaloth target = new RumblingBaloth();
        prepareAbility(List.of(target));
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Rumbling Baloth");
        harness.assertInGraveyard(player1, "Corpse Hauler");
    }

    private void prepareAbility(List<Card> graveyard) {
        harness.addToBattlefield(player1, new CorpseHauler());
        harness.setGraveyard(player1, graveyard);
        addAbilityMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
