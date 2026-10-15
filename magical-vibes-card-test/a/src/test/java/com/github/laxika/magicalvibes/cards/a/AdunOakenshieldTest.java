package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdunOakenshield.class, BarbaryApes.class, Boomerang.class})
class AdunOakenshieldTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from the graveyard to hand")
    void returnsTargetCreatureCardToHand() {
        Permanent adun = addCreatureReady(player1, new AdunOakenshield());
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(adun.isTapped()).isTrue();
        harness.assertInHand(player1, "Barbary Apes");
        harness.assertNotInGraveyard(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        addCreatureReady(player1, new AdunOakenshield());
        Card noncreature = new Boomerang();
        harness.setGraveyard(player1, List.of(noncreature));
        addAbilityMana(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetCreatureCardInOpponentsGraveyard() {
        addCreatureReady(player1, new AdunOakenshield());
        Card creature = new BarbaryApes();
        harness.setGraveyard(player2, List.of(creature));
        addAbilityMana(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the required colored mana")
    void cannotActivateWithoutRequiredMana() {
        addCreatureReady(player1, new AdunOakenshield());
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new AdunOakenshield());
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent adun = addCreatureReady(player1, new AdunOakenshield());
        adun.tap();
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a target")
    void cannotActivateWithoutTarget() {
        addCreatureReady(player1, new AdunOakenshield());
        harness.setGraveyard(player1, List.of(new BarbaryApes()));
        addAbilityMana(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability still returns its target after Adun leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent adun = addCreatureReady(player1, new AdunOakenshield());
        Card creature = new BarbaryApes();
        harness.setGraveyard(player1, List.of(creature));
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, adun.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Adun Oakenshield");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Barbary Apes");
        harness.assertNotInGraveyard(player1, "Barbary Apes");
    }

    @Test
    @DisplayName("Does not return another creature when the target leaves the graveyard")
    void doesNotChooseReplacementForMissingTarget() {
        Permanent adun = addCreatureReady(player1, new AdunOakenshield());
        Card target = new BarbaryApes();
        Card other = new BarbaryApes();
        harness.setGraveyard(player1, List.of(target, other));
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(other));

        harness.passBothPriorities();

        assertThat(adun.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Barbary Apes");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    private void addAbilityMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
