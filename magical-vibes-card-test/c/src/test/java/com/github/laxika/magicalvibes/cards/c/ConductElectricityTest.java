package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConductElectricity.class, ThreeTreeMascot.class})
class ConductElectricityTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to a creature and 2 damage to a creature token")
    void dealsDamageToBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creature("Creature", 10, 10, false));
        Permanent token = harness.addToBattlefieldAndReturn(player2, creature("Creature Token", 5, 5, true));

        cast(List.of(creature.getId(), token.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(6);
        assertThat(token.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("May omit the creature-token target")
    void mayOmitTokenTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creature("Creature", 10, 10, false));

        cast(List.of(creature.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Rejects a nontoken creature as the creature-token target")
    void rejectsNontokenCreatureAsSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creature("Creature", 10, 10, false));
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, creature("Second Creature", 10, 10, false));
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature token");
    }

    @Test
    @DisplayName("May choose the same creature token for both targets")
    void mayChooseSameTokenForBothTargets() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, creature("Creature Token", 10, 10, true));

        cast(List.of(token.getId(), token.getId()));

        assertThat(token.getMarkedDamage()).isEqualTo(8);
    }

    @Test
    @DisplayName("Still deals 2 damage to the token when the first target leaves")
    void resolvesTokenDamageWhenFirstTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThreeTreeMascot());
        Permanent token = harness.addToBattlefieldAndReturn(player2, creature("Creature Token", 5, 5, true));
        prepareCast();
        harness.castInstant(player1, 0, List.of(creature.getId(), token.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(token.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Conduct Electricity");
    }

    @Test
    @DisplayName("Still deals 6 damage to the creature when the token target leaves")
    void resolvesCreatureDamageWhenTokenTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creature("Creature", 10, 10, false));
        Card tokenCard = new ThreeTreeMascot();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        prepareCast();
        harness.castInstant(player1, 0, List.of(creature.getId(), token.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(token);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Conduct Electricity");
    }

    @Test
    @DisplayName("Lethal damage destroys the creature and removes the creature token")
    void lethalDamageRemovesBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        Card tokenCard = new ThreeTreeMascot();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        cast(List.of(creature.getId(), token.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        harness.assertInGraveyard(player1, "Three Tree Mascot");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a token only as the first target deals exactly 6 damage")
    void tokenAsOnlyTargetReceivesSixDamage() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, creature("Creature Token", 10, 10, true));

        cast(List.of(token.getId()));

        assertThat(token.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not resolve when both targets leave the battlefield")
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThreeTreeMascot());
        Card tokenCard = new ThreeTreeMascot();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        prepareCast();
        harness.castInstant(player1, 0, List.of(creature.getId(), token.getId()));

        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(creature, token));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(token.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Conduct Electricity");
    }

    @Test
    @DisplayName("Requires the first creature target even though the token target is optional")
    void cannotCastWithoutTargets() {
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targets) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ConductElectricity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private static Card creature(String name, int power, int toughness, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(token);
        return card;
    }
}
