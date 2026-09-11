package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.t.ThranTome;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesKiss.class, BenalishKnight.class, ThranTome.class})
class NaturesKissTest extends BaseCardTest {

    private Permanent attachKissTo(Permanent host) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new NaturesKiss());
        auraPerm.setAttachedTo(host.getId());
        return auraPerm;
    }

    @Test
    @DisplayName("Activating gives the enchanted creature +1/+1 and exiles the top graveyard card")
    void activatedAbilityBoostsAndExiles() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());
        attachKissTo(knight);
        Card remainingCard = new ThranTome();
        Card topCard = new ThranTome();
        harness.setGraveyard(player1, List.of(remainingCard, topCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == topCard);
    }

    @Test
    @DisplayName("Boost stacks across multiple activations")
    void boostStacks() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());
        attachKissTo(knight);
        harness.setGraveyard(player1, List.of(new ThranTome(), new ThranTome()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());
        attachKissTo(knight);
        harness.setGraveyard(player1, List.of(new ThranTome()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be activated with an empty graveyard")
    void requiresNonEmptyGraveyard() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());
        attachKissTo(knight);
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranTome());
        harness.setHand(player1, List.of(new NaturesKiss()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant a creature when cast")
    void castsOntoCreature() {
        Permanent knight = addCreatureReady(player1, new BenalishKnight());
        harness.setHand(player1, List.of(new NaturesKiss()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof NaturesKiss
                        && knight.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Uses its controller's graveyard when enchanting an opponent's creature")
    void usesControllerGraveyardForOpponentCreature() {
        Permanent opponentKnight = addCreatureReady(player2, new BenalishKnight());
        attachKissTo(opponentKnight);
        Card controllerCard = new ThranTome();
        Card opponentCard = new ThranTome();
        harness.setGraveyard(player1, List.of(controllerCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentKnight)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == controllerCard);
    }
}
