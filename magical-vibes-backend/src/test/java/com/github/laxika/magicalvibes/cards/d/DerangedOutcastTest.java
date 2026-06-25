package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class DerangedOutcastTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Has one activated ability: {1}{G}, Sacrifice a Human: put two +1/+1 counters on target creature")
    void hasCorrectAbility() {
        DerangedOutcast card = new DerangedOutcast();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}{G}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSubtypeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);

        SacrificeSubtypeCreatureCost sacCost = (SacrificeSubtypeCreatureCost) ability.getEffects().get(0);
        assertThat(sacCost.subtype()).isEqualTo(CardSubtype.HUMAN);

        PutPlusOnePlusOneCounterOnTargetCreatureEffect counterEffect =
                (PutPlusOnePlusOneCounterOnTargetCreatureEffect) ability.getEffects().get(1);
        assertThat(counterEffect.count()).isEqualTo(2);
    }

    // ===== Sacrifice a Human, put two +1/+1 counters on target =====

    @Test
    @DisplayName("Sacrificing a Human puts two +1/+1 counters on target creature; the Outcast survives")
    void sacrificeHumanPutsTwoCountersOnTarget() {
        Permanent outcast = addReadyOutcast(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, createHumanToken());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Two Humans exist (Outcast + token) → prompted to choose the sacrifice.
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Human"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(outcast);
    }

    @Test
    @DisplayName("Can sacrifice the Outcast itself when it is the only Human")
    void canSacrificeItselfWhenOnlyHuman() {
        Permanent outcast = addReadyOutcast(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Outcast is the only Human → auto-sacrifices itself.
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deranged Outcast"));
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        addReadyOutcast(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Outcast is the only Human → auto-sacrifices itself.
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    // ===== Mana cost required =====

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        addReadyOutcast(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Only {G} — need {1}{G}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyOutcast(Player player) {
        DerangedOutcast card = new DerangedOutcast();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createHumanToken() {
        Card card = new Card();
        card.setName("Human");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        return card;
    }
}
