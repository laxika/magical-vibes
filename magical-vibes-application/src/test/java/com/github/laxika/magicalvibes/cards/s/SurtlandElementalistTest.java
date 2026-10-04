package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.h.WaywardGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurtlandElementalist.class, WaywardGiant.class, Divination.class})
class SurtlandElementalistTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Giant in hand it requires the additional {2}")
    void requiresAdditionalManaWithoutGiant() {
        harness.setHand(player1, List.of(new SurtlandElementalist()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {2} can be paid when no Giant is revealed")
    void paysAdditionalManaWithoutGiant() {
        SurtlandElementalist elementalist = new SurtlandElementalist();
        harness.setHand(player1, List.of(elementalist));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(elementalist.getId()));
    }

    @Test
    @DisplayName("A Giant in hand lets it be cast without the additional {2}")
    void revealsGiantToAvoidAdditionalMana() {
        SurtlandElementalist elementalist = new SurtlandElementalist();
        WaywardGiant giant = new WaywardGiant();
        harness.setHand(player1, List.of(elementalist, giant));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(elementalist.getId()));
        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("When it attacks, it may cast an instant or sorcery from hand for free")
    void attacksMayCastInstantOrSorceryForFree() {
        Divination spell = new Divination();
        harness.setHand(player1, new ArrayList<>(List.of(spell)));
        addAttacker();

        advanceToAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("When it attacks, it does not offer a creature from hand")
    void attacksDoNotOfferCreature() {
        harness.setHand(player1, new ArrayList<>(List.of(new WaywardGiant())));
        addAttacker();

        advanceToAttackTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SurtlandElementalist());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private void advanceToAttackTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
