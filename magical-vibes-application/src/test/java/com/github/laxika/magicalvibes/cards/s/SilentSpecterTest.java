package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilentSpecter.class, GrizzlyBears.class})
class SilentSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard two chosen cards")
    void combatDamageMakesDamagedPlayerDiscardTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        Permanent specter = addReadyCreature(player1, new SilentSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A blocked Silent Specter does not make the defending player discard")
    void blockedSpecterDoesNotTrigger() {
        List<Card> hand = new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, hand);
        Permanent specter = addReadyCreature(player1, new SilentSpecter());
        specter.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsAll(hand);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Silent Specter's discard ability")
    void noncombatDamageDoesNotTrigger() {
        SilentSpecter card = new SilentSpecter();
        card.addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)), "{T}: This creature deals 1 damage to any target."));
        Permanent specter = addReadyCreature(player1, card);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(specter),
                null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
